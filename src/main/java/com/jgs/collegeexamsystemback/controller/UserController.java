package com.jgs.collegeexamsystemback.controller;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Announcement;
import com.jgs.collegeexamsystemback.pojo.User;
import com.jgs.collegeexamsystemback.service.UserService;
import com.jgs.collegeexamsystemback.util.MinioFileUtil;
import com.jgs.collegeexamsystemback.vo.UpdatePasswordVo;
import com.jgs.collegeexamsystemback.vo.UpdateUserVo;
import com.jgs.collegeexamsystemback.vo.UserQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
/**
 * @author Administrator
 * @version 1.0
 * @description 用户控制类
 * @date 2023/7/19 0019 20:39
 */
@RestController
@RequestMapping("user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisCache redisCache;

    /**
    * @description 根据条件查询用户分页列表
    * @author Administrator
    * @date  20:58
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("getUserList/{pageNo}/{pageSize}")
    public Result getUserPageList(@PathVariable int pageNo, @PathVariable int pageSize, @RequestBody UserQueryVo userQueryVo){
        Page<User> userPageList = userService.getUserPageList(pageNo,pageSize,userQueryVo);
        return Result.ok(userPageList);
    }

    /**
    * @description 添加用户
    * @author Administrator
    * @date  21:24
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("saveUser")
    public Result saveUser(@RequestBody User user) throws Exception {
        return userService.saveUser(user);
    }

    /**
    * @description 根据用户id获取用户信息
    * @author Administrator
    * @date  9:49
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @PostMapping("getUserInfo/{userId}")
    public Result getUserInfo(@PathVariable Long userId){
        if (userId != null){
            User user = userService.getById(userId);
            user.setPassword(null);
            return new Result(200,"获取用户信息成功！",user);
        }
        return new Result(201,"获取用户信息失败！");
    }

    /**
    * @description 根据id删除用户
    * @author Administrator
    * @date  21:52
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteUser/{id}")
    public Result deleteUserById(@PathVariable Integer id){
        if (id == null){
            return new Result(201,"删除失败，请重试！");
        }
        boolean result = userService.removeById(id);
        if (result){
            redisCache.deleteObject("user");
            return Result.ok("删除成功！");
        }
        return new Result(201,"删除失败,请检查网络或刷新！");
    }

    /**
    * @description 批量删除用户
    * @author Administrator
    * @date  14:09
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteUsers")
    public Result deleteUsers(@RequestBody List<Integer> userIds){
        return userService.deleteUsers(userIds);
    }

    /**
     * 删除所有用户
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteAll")
    public Result deleteAll() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return userService.deleteAll();
    }

    /**
    * @description 管理员修改用户信息
    * @author Administrator
    * @date  21:56
    */
    @PreAuthorize("hasAnyAuthority('管理员')")
    @PostMapping("updateUser")
    public Result updateUser(@RequestBody UpdateUserVo updateUserVo){
        return userService.updateUser(updateUserVo);
    }

    /**
    * @description 用户更新信息
    * @author Administrator
    * @date  22:15
    */
    @PreAuthorize("hasAnyAuthority('管理员','教师','学生')")
    @PostMapping("updateUserInfo")
    public Result updateUserInfo(@RequestBody User user) throws Exception{
        return userService.updateUserInfo(user);
    }

    /**
     * 取消用户更新操作，删除minio存储的图片
     */
    @PreAuthorize("hasAnyAuthority('管理员','教师','学生')")
    @GetMapping("cancelUpdateUserInfo")
    public Result cancelUpdateUserInfo(@RequestParam("imageUrl")String imageUrl) throws Exception{
        if (imageUrl.isEmpty()){
            return Result.ok("图片地址为空!");
        }
        boolean removeImage = new MinioFileUtil().removeImage(imageUrl);
        if (removeImage){
            return Result.ok("图片删除成功!");
        }
        return Result.fail("操作失败!");
    }

    /**
    * @description 用户更新密码
    * @author Administrator
    * @date  14:09
    */
    @PreAuthorize("hasAnyAuthority('管理员','教师','学生')")
    @PostMapping("updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordVo updatePasswordVo){
        return userService.updatePassword(updatePasswordVo);
    }

    /**
    * @description 修改用户状态
    * @author Administrator
    * @date  9:40
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("updateUserStatus/{userId}/{status}")
    public Result updateUserStatus(@PathVariable Integer userId, @PathVariable String status){
        return userService.updateUserStatus(userId,status);
    }

    /**
    * @description 用户头像上传
    * @author Administrator
    * @date  17:11
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @RequestMapping("uploadAvatar")
    public Result uploadAvatar(@RequestParam("file")MultipartFile file) throws Exception {
        return userService.uploadAvatar(file);
    }

    /**
    * @description 用户获取头像
    * @author Administrator
    * @date  20:06
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("getAvatar/{userId}")
    public Result getAvatar(@PathVariable Integer userId){
        return userService.getAvatar(userId);
    }

    /**
    * @description 获取总用户数
    * @author Administrator
    * @date  12:27
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getUsers")
    public Result getUsers(){
        List<User> users = userService.getBaseMapper().selectList(null);
        return Result.ok(users.size());
    }

    /**
    * @description 获取用户总访问量
    * @author Administrator
    * @date  15:09
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getUserAccesses")
    public Result getUserAccesses(){
        return userService.getUserAccesses();
    }

    /**
    * @description 获取每日用户访问
    * @author Administrator
    * @date  20:18
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getTodayAccesses/{date}")
    public Result getTodayAccesses(@PathVariable Date date){
        return userService.getTodayAccesses(date);
    }

    /**
    * @description 获取通知列表
    * @author Administrator
    * @date  20:18
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("getAnnouncements")
    public Result getAnnouncements(){
        return userService.getAnnouncements();
    }

    /**
    * @description 获取更多通知
    * @author Administrator
    * @date  22:20
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("getMoreAnnouncements")
    public Result getMoreAnnouncements(){
        return userService.getMoreAnnouncements();
    }

    /**
    * @description 删除通知
    * @author Administrator
    * @date  22:36
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteAnnouncement")
    public Result deleteAnnouncement(@RequestBody Announcement announcement){
        return userService.deleteAnnouncement(announcement);
    }
    /**
    * @description 管理员发布通知
    * @author Administrator
    * @date  20:19
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("publishAnnouncement")
    public Result publishAnnouncement(@RequestParam String announcement){
        return userService.publishAnnouncement(announcement);
    }

    /**
     * 根据userId获取Gitee账户
     */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("getGiteeByUserId/{userId}")
    public Result getGiteeByUserId(@PathVariable Long userId){
        if (userId != null){
            String gitee = userService.getGiteeByUserId(userId);
            return new Result(200,"获取成功!",gitee);
        }
        return Result.fail("获取失败!");
    }

    /**
    * @description 解绑Gitee
    * @author Administrator
    * @date  13:59
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("unbindGitee/{userId}")
    public Result unbindGitee(@PathVariable Integer userId){
        return userService.unbindGitee(userId);
    }

    /**
    * @description 绑定Gitee
    * @author Administrator
    * @date  20:19
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("bindGitee/{userId}/{gitee}")
    public Result bindGitee(@PathVariable Integer userId,@PathVariable String gitee){
        return userService.bindGitee(userId,gitee);
    }

    /**
     * 用户excel文件接收
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("receiveExcel")
    public Result receiveExcel(@RequestParam("file") MultipartFile file){
        try{
            // 使用WorkbookFactory校验文件是否为excel文件
            Workbook sheets = WorkbookFactory.create(file.getInputStream());
            if (sheets != null){
                // 解析excel文件
                List<List<Object>> lists = ExcelUtil.getReader(file.getInputStream()).read(2);
                sheets.close();
                // 将数据存入数据库
                return userService.saveUsers(lists);
            }
            return Result.fail("接收失败！");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("文件格式错误！");
        }
    }
}
