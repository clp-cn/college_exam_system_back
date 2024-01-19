package com.jgs.collegeexamsystemback.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.LoginUser;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.AccessMapper;
import com.jgs.collegeexamsystemback.mapper.AnnouncementMapper;
import com.jgs.collegeexamsystemback.mapper.UserMapper;
import com.jgs.collegeexamsystemback.pojo.Access;
import com.jgs.collegeexamsystemback.pojo.Announcement;
import com.jgs.collegeexamsystemback.pojo.User;
import com.jgs.collegeexamsystemback.service.UserService;
import com.jgs.collegeexamsystemback.token.GitAuthenticationToken;
import com.jgs.collegeexamsystemback.token.SmsAuthenticationToken;
import com.jgs.collegeexamsystemback.util.GetTemPasswordUtil;
import com.jgs.collegeexamsystemback.util.JwtUtil;
import com.jgs.collegeexamsystemback.util.MinioFileUtil;
import com.jgs.collegeexamsystemback.util.SendMailUtil;
import com.jgs.collegeexamsystemback.vo.*;
import io.jsonwebtoken.Claims;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* @author Administrator
* @description UserServiceImpl
* @createDate 2023-06-30 13:00:21
*/
@Service
@Transactional
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    @Resource
    private RedisCache redisCache;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private AccessMapper accessMapper;
    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    @Resource
    private AnnouncementMapper announcementMapper;

    // 用户登录
    @Override
    public Result login(LoginVo loginVo) {
        // AuthenticationManager authenticate进行用户认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginVo.getUsername(), loginVo.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        // 如果认证没通过，给出对应的提示
        if (ObjectUtil.isNull(authenticate)){
            throw new RuntimeException("用户认证失败");
        }
        // 如果认证通过了，使用userId生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        return saveRedisLoginUser(loginUser);
    }

    // 获取用户信息
    @Override
    public Result getUserInfo(String token) {
        LoginUser loginUser = redisCache.getCacheObject("login_token:" + JwtUtil.parseJwt(token).getId());
        User user = loginUser.getUser();
        user.setPassword(null);
        if (ObjectUtils.isNotNull(user)){
            return Result.ok(user);
        }
        return Result.fail("未查询到用户信息！");
    }

    // 用户退出登录
    @Override
    public Result logout() {
        // 获取SecurityContextHolder中的用户
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Claims principal = (Claims) authenticationToken.getPrincipal();
        LoginUser loginUser = redisCache.getCacheObject("login_token:" + principal.getId());
        User user = loginUser.getUser();
        // 更新singleLogin为0（未登录）
        user.setSingleLogin(0);
        int update = baseMapper.updateById(user);
        if (update == 0){
            return new Result(403,"退出登录失败！");
        }
        // 获取redisKey,删除redis值
        boolean result = redisCache.deleteObject("login_token:" + principal.getId());
        if (result){
            return new Result(200,"退出登录成功,即将跳转到登录页面！");
        }
        return new Result(403,"退出登录失败！");
    }

    // 根据条件查询用户分页列表
    @Override
    public Page<User> getUserPageList(int pageNo,int pageSize, UserQueryVo userQueryVo) {
        String redisKey = pageNo + "_" + pageSize + userQueryVo.getUsername() + userQueryVo.getNickname() + userQueryVo.getRoles();
        Page<User> userPageList = redisCache.getCacheMapValue("user", redisKey);
        if (userPageList == null){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("username",userQueryVo.getUsername())
                    .like("nickname",userQueryVo.getNickname())
                    .like("roles",userQueryVo.getRoles())
                    .notIn("roles","管理员");
            Page<User> userPage = baseMapper.selectPage(new Page<>(pageNo,pageSize), queryWrapper);
            List<User> users = userPage.getRecords();
            users.forEach(user -> {
                user.setPassword(null);
            });
            userPage.setRecords(users);
            redisCache.setCacheMapValue("user",redisKey,userPage);
            return userPage;
        }
        return userPageList;
    }

    // 手机号登录
    @Override
    public Result loginPhone(String phone,String code) {
        SmsAuthenticationToken authenticationToken = new SmsAuthenticationToken(phone,code);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        redisCache.deleteObject(phone);
        if (ObjectUtil.isNull(authenticate)) {
            throw new RuntimeException("用户认证失败");
        }
        String principal = (String) authenticate.getPrincipal();
        User user = baseMapper.selectOne(new QueryWrapper<User>().eq("telephone", principal));
        if (user == null){
            return new Result(201,"该手机号暂未绑定用户！");
        }
        LoginUser loginUser = getLoginUser(user);
        return saveRedisLoginUser(loginUser);
    }

    // git登录
    @Override
    public Result loginGit(String gitee) {
        GitAuthenticationToken authenticationToken = new GitAuthenticationToken(gitee);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (ObjectUtil.isNull(authenticate)) {
            throw new RuntimeException("用户认证失败");
        }
        String principal = (String) authenticate.getPrincipal();
        User user = baseMapper.selectOne(new QueryWrapper<User>().eq("gitee", principal));
        if (user == null){
            return new Result(201,"该Gitee账号暂未绑定用户！");
        }
        LoginUser loginUser = getLoginUser(user);
        return saveRedisLoginUser(loginUser);
    }

    // 重置密码
    @Override
    public Result resetPassword(String email) throws MessagingException, UnsupportedEncodingException {
        if (email == null){
            return new Result(201,"邮箱不能为空！");
        }
        User user = baseMapper.selectOne(new QueryWrapper<User>().eq("qq_email", email));
        if (!ObjectUtil.isNull(user)){
            // 用户存在，给用户发送邮件，包含临时密码
            SendMailUtil sendMailUtil = new SendMailUtil();
            // 生成临时随机密码
            String temPassword = new GetTemPasswordUtil().getTemPassword();
            // 更新用户密码
            user.setPassword(new BCryptPasswordEncoder().encode(temPassword));
            int result = baseMapper.updateById(user);
            if (result > 0){
                sendMailUtil.sendMail(email,temPassword);
                return new Result(200,"密码重置成功！");
            }
        }
        return new Result(201,"该账号不存在！");
    }

    // 添加用户
    @Override
    public Result saveUser(User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (user == null){
            return Result.fail();
        }
        User saveUser = commonSaveUser(user);
        // 先对用户名进行检查，保证用户名唯一
        User selectUser = baseMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (!ObjectUtil.isNull(selectUser)){
            return new Result(201,"添加用户失败,请换一个用户名");
        }
        int result = baseMapper.insert(saveUser);
        if (result > 0){
            redisCache.deleteObject("user");
            return Result.ok("添加用户成功！");
        }
        return Result.fail();
    }

    // 绑定Gitee
    @Override
    public Result bindGitee(Integer userId, String gitee) {
        if (userId == null || gitee == null){
            return new Result(201,"绑定失败");
        }
        List<User> users = baseMapper.selectList(new QueryWrapper<User>().eq("gitee", gitee));
        if (users == null || !users.isEmpty()){
            return new Result(201,"绑定失败，该Gitee账号已绑定用户，请换一个Gitee账号");
        }
        User user = baseMapper.selectById(userId);
        user.setGitee(gitee);
        int update = baseMapper.updateById(user);
        if (update > 0){
            return new Result(200,"绑定成功");
        }
        return new Result(201,"绑定失败");
    }

    // 解绑Gitee
    @Override
    public Result unbindGitee(Integer userId) {
        if (userId == null){
            return new Result(201,"解绑失败");
        }
        User user = baseMapper.selectById(userId);
        int update = baseMapper.update(user, new UpdateWrapper<User>().eq("user_id", userId).set("gitee", null));
        if (update > 0){
            return new Result(200,"解绑成功！");
        }
        return new Result(201,"解绑失败！");
    }

    // 管理员发布公告
    @Override
    public Result publishAnnouncement(String announcement) {
        if (announcement == null){
            return new Result(201,"发布失败，公告不能为空！");
        }
        Announcement selectOne = announcementMapper.selectOne(new QueryWrapper<Announcement>().eq("announcement", announcement));
        if (!ObjectUtil.isNull(selectOne)){
            return new Result(201,"请勿重复发布！");
        }
        Announcement insert = new Announcement();
        insert.setAnnouncement(announcement);
        insert.setCreateTime(new Date());
        int result = announcementMapper.insert(insert);
        if (result > 0){
            return new Result(200,"发布成功！");
        }
        return new Result(201,"发布失败，请重试！");
    }

    // 获取公告列表
    @Override
    public Result getAnnouncements() {
        List<Announcement> announcements = announcementMapper.selectList(new QueryWrapper<Announcement>().orderByDesc("create_time").last("limit 3"));
        return Result.ok(announcements);
    }

    // 获取每日用户访问量
    @Override
    public Result getTodayAccesses(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(date);
        Access accessDate = accessMapper.selectOne(new QueryWrapper<Access>().eq("access_date", format));
        return Result.ok(accessDate.getNumber());
    }

    // 获取用户总访问量
    @Override
    public Result getUserAccesses() {
        Map<String, Object> map = getMap(new QueryWrapper<User>().select("IFNULL(sum(access),0) as accesses"));
        Object accesses = map.get("accesses");
        return Result.ok(accesses);
    }

    // 获取用户头像
    @Override
    public Result getAvatar(Integer userId) {
        if (userId == null){
            return new Result(201,"获取头像失败！");
        }
        User user = baseMapper.selectById(userId);
        if (user != null){
            return Result.ok(user.getAvatar());
        }
        return new Result(201,"获取头像失败！");
    }

    // 用户头像上传
    @Override
    public Result uploadAvatar(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 判断图片是否为空
        if (ObjectUtil.isNull(file)){
            return new Result(201,"图片为空!");
        }
        // 图片地址
        String imageUrl = new MinioFileUtil().uploadImage(file);
        if (!imageUrl.isEmpty()){
            return new Result(200,"图片上传成功!",imageUrl);
        }
        return Result.fail("图片上传失败!");
    }

    // 修改用户状态
    @Override
    public Result updateUserStatus(Integer userId, String status) {
        User user = baseMapper.selectById(userId);
        if ("正常".equals(status)){
            user.setStatus(0);
            int result = baseMapper.updateById(user);
            if (result > 0){
                redisCache.deleteObject("user");
                return Result.ok();
            }
        }else {
            user.setStatus(1);
            int result = baseMapper.updateById(user);
            if (result > 0){
                redisCache.deleteObject("user");
                return Result.ok();
            }
        }
        return Result.fail();
    }

    // 用户更新密码
    @Override
    public Result updatePassword(UpdatePasswordVo updatePasswordVo) {
        User user = baseMapper.selectById(updatePasswordVo.getUserId());
        boolean matches = new BCryptPasswordEncoder().matches(updatePasswordVo.getOldPassword().trim(), user.getPassword());
        if (!matches){
            return new Result(201,"原密码错误，请重试！");
        }
        if (!updatePasswordVo.getNewPassword().equals(updatePasswordVo.getConfirmPassword().trim())){
            return new Result(201,"两次密码输入不一致，请重试！");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(updatePasswordVo.getNewPassword().trim()));
        int result = baseMapper.updateById(user);
        if (result > 0){
            return new Result(200,"更新密码成功！");
        }
        return new Result(201,"更新密码失败，请重试！");
    }

    // 用户更新信息
    @Override
    public Result updateUserInfo(User user) {
        User selectUser = baseMapper.selectById(user.getUserId());
        if (user.getUsername() != null){
            List<User> selectList = baseMapper.selectList(new QueryWrapper<User>().eq("username", user.getUsername()));
            if (selectList != null && selectList.size() > 1){
                return new Result(201,"更新失败，用户名已存在，请换一个用户名！");
            }
            selectUser.setUsername(user.getUsername().trim());
        }
        String avatar = selectUser.getAvatar();
        if (user.getAvatar() != null){
            selectUser.setAvatar(user.getAvatar());
        }
        selectUser.setGender(user.getGender());
        if (user.getQq() != null){
            selectUser.setQq(user.getQq());
        }
        if (user.getNickname() != null){
            selectUser.setNickname(user.getNickname().trim());
        }
        if (user.getQqEmail() != null){
            selectUser.setQqEmail(user.getQqEmail());
        }
        if (user.getTelephone() != null){
            selectUser.setTelephone(user.getTelephone());
        }
        int result = baseMapper.updateById(selectUser);
        if (result > 0){
            if(user.getAvatar() != null){
                try{
                    new MinioFileUtil().removeImage(avatar);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            redisCache.deleteObject("user");
            return new Result(200,"更新成功！");
        }
        return new Result(201,"操作失败，请刷新重试！");
    }

    // 管理员更新用户信息
    @Override
    public Result updateUser(UpdateUserVo updateUserVo) {
        User user = baseMapper.selectById(updateUserVo.getUserId());
        user.setUsername(updateUserVo.getUsername());
        if (updateUserVo.getPassword() != null){
            // 对用户密码进行加密
            String newPassword = passwordEncoder.encode(updateUserVo.getPassword());
            user.setPassword(newPassword);
        }
        user.setGender(updateUserVo.getGender());
        user.setRoles(updateUserVo.getRoles());
        user.setNickname(updateUserVo.getNickname());
        // 根据用户名查询是否存在用户，避免用户名重复
        User selectUser = baseMapper.selectOne(new QueryWrapper<User>().eq("username", updateUserVo.getUsername()));
        if (!ObjectUtil.isNull(selectUser) && !ObjectUtil.equal(updateUserVo.getUserId(),selectUser.getUserId())){
            return Result.fail("更新失败,请换一个用户名！");
        }
        int result = baseMapper.updateById(user);
        if (result > 0){
            redisCache.deleteObject("user");
            return Result.ok("更新成功!");
        }
        return Result.fail();
    }

    // 批量删除用户
    @Override
    public Result deleteUsers(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()){
            return new Result(201,"请选择要删除的用户！");
        }
        int result = baseMapper.deleteBatchIds(userIds);
        if (result > 0){
            redisCache.deleteObject("user");
            return Result.ok("删除成功！");
        }
        return new Result(201,"删除失败,请检查网络或刷新！");
    }

    // 获取更多通知
    @Override
    public Result getMoreAnnouncements() {
        return Result.ok(announcementMapper.selectList(null));
    }

    // 删除通知
    @Override
    public Result deleteAnnouncement(Announcement announcement) {
        if (announcement == null){
            return Result.fail();
        }
        int delete = announcementMapper.deleteById(announcement);
        if (delete > 0){
            return Result.ok();
        }
        return Result.fail();
    }

    // 导入文件批量保存用户
    @Override
    public Result saveUsers(List<List<Object>> lists) {
        if (lists == null || lists.isEmpty()){
            return new Result(201,"导入失败，文件内容为空！");
        }
        List<User> users = new ArrayList<>();
        lists.forEach(data -> {
            User user = new User();
            user.setUsername(data.get(1).toString());
            user.setPassword(data.get(2).toString());
            user.setGender(data.get(3));
            user.setRoles(data.get(4));
            User saveUser = null;
            try {
                saveUser = commonSaveUser(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
            User selectUser = baseMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
            if (selectUser == null){
                users.add(saveUser);
            }
        });
        if (users.isEmpty()){
            return Result.fail("导入失败，文件内容已存在！");
        }
        boolean result = this.saveBatch(users, users.size());
        if(!result){
            return Result.fail("导入失败！");
        }
        redisCache.deleteObject("user");
        return Result.ok("导入成功！");
    }

    // 根据userId获取Gitee账户
    @Override
    public String getGiteeByUserId(Long userId) {
        return baseMapper.selectById(userId).getGitee();
    }

    // 删除所有用户
    @Override
    public Result deleteAll() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.notIn("roles","管理员");
        List<User> userList = baseMapper.selectList(wrapper);
        List<String> imageUrls = new ArrayList<>();
        userList.forEach(user -> {
            imageUrls.add(user.getAvatar());
        });
        int delete = baseMapper.delete(wrapper);
        if (delete > 0){
            boolean removeResult = new MinioFileUtil().removeImages(imageUrls);
            if (removeResult){
                redisCache.deleteObject("user");
                return Result.ok();
            }
        }
        return Result.fail();
    }

    // 代码抽取
    private Result saveRedisLoginUser(LoginUser loginUser){
        Integer singleLogin = loginUser.getUser().getSingleLogin();
        if (singleLogin == 1){
            return new Result(201,"登录失败，该账号已在其它地方登录！");
        }
        String token = JwtUtil.createJwt(loginUser.getUser().toString(), (long) (20 * 60 * 60 * 1000));
        // 将完整的用户信息存入redis,jwt id作为key
        redisCache.setCacheObject("login_token:" + JwtUtil.parseJwt(token).getId(), loginUser);
        // 用户访问次数+1
        loginUser.getUser().setAccess(loginUser.getUser().getAccess() + 1);
        // 单端登录设置为1（已登录）
        loginUser.getUser().setSingleLogin(1);
        int result = baseMapper.updateById(loginUser.getUser());
        if (result == 0){
            redisCache.deleteObject("login_token:" + JwtUtil.parseJwt(token).getId());
            return new Result(201,"登录失败！");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date());
        Access accessDate = accessMapper.selectOne(new QueryWrapper<Access>().eq("access_date", format));
        if (accessDate != null) {
            accessDate.setNumber(accessDate.getNumber() + 1);
            accessMapper.updateById(accessDate);
        } else {
            Access access = new Access();
            access.setAccessDate(format);
            access.setNumber(1);
            accessMapper.insert(access);
        }
        return new Result(200, "登录成功！", token);
    }

    private LoginUser getLoginUser(User user){
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        Set<String> permissions = new HashSet<>();
        permissions.add((String) user.getRoles());
        loginUser.setPermissions(permissions);
        return loginUser;
    }

    private User commonSaveUser(User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 先对用户密码进行加密
        String newPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(newPassword);
        // 设置默认头像
        String imageUrl = new MinioFileUtil().uploadDefaultImage();
        user.setAvatar(imageUrl);
        // 设置随机昵称
        user.setNickname("school_" + RandomUtil.randomString(10));
        // 设置默认状态
        user.setStatus(0);
        user.setAccess(0);
        return user;
    }

}




