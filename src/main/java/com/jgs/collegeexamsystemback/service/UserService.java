package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Announcement;
import com.jgs.collegeexamsystemback.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.vo.*;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description UserService
* @createDate 2023-06-30 13:00:21
*/
public interface UserService extends IService<User> {

    // 用户登录
    Result login(LoginVo loginVo);

    // 获取用户信息
    Result getUserInfo(String token);

    // 用户退出
    Result logout();

    // 根据条件查询用户分页列表
    Page<User> getUserPageList(int pageNo,int pageSize, UserQueryVo userQueryVo);

    // 手机号登录
    Result loginPhone(String phone,String code);

    // Git登录
    Result loginGit(String gitee);

    // 重置密码
    Result resetPassword(String email) throws MessagingException, UnsupportedEncodingException;

    // 添加用户
    Result saveUser(User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    // 绑定Gitee
    Result bindGitee(Integer userId, String gitee);

    // 解绑Gitee
    Result unbindGitee(Integer userId);

    // 管理员发布公告
    Result publishAnnouncement(String announcement);

    // 获取公告列表
    Result getAnnouncements();

    // 获取每日用户访问量
    Result getTodayAccesses(Date date);

    // 获取用户总访问量
    Result getUserAccesses();

    // 获取用户头像
    Result getAvatar(Integer userId);

    // 用户头像上传
    Result uploadAvatar(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    // 修改用户状态
    Result updateUserStatus(Integer userId, String status);

    // 用户更新密码
    Result updatePassword(UpdatePasswordVo updatePasswordVo);

    // 用户更新信息
    Result updateUserInfo(User user) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    // 管理员更新用户信息
    Result updateUser(UpdateUserVo updateUserVo);

    // 批量删除用户
    Result deleteUsers(List<Integer> userIds);

    // 获取更多通知
    Result getMoreAnnouncements();

    // 删除通知
    Result deleteAnnouncement(Announcement announcement);

    // 导入文件批量保存用户
    Result saveUsers(List<List<Object>> lists);

    // 根据userId获取Gitee账户
    String getGiteeByUserId(Long userId);

    // 删除所有用户
    Result deleteAll() throws IOException, NoSuchAlgorithmException, InvalidKeyException;
}
