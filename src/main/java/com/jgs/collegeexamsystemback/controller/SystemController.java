package com.jgs.collegeexamsystemback.controller;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.service.UserService;
import com.jgs.collegeexamsystemback.util.ChatGPTUtil;
import com.jgs.collegeexamsystemback.util.VerifyCodeImageUtil;
import com.jgs.collegeexamsystemback.vo.LoginVo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
/**
 * @author Administrator
 * @version 1.0
 * @description 系统控制类
 * @date 2023/6/30 0030 13:16
 */
@RestController
@RequestMapping("system")
public class SystemController {
    @Resource
    private UserService userService;
    @Resource
    private RedisCache redisCache;

    /**
    * @description 微信小程序获取验证码
    * @author Administrator
    * @date  21:53
    */
    @GetMapping("wx/getVerifyCode")
    public Result getWXVerifyCode(HttpServletRequest request){
//        BufferedImage verifyCodeImage = VerifyCodeImageUtil.getVerifyCodeImage();
        String verifyCode = String.valueOf(VerifyCodeImageUtil.getVerifyCode());
        // 根据ip将验证码存入redis,设置过期时间1h
        redisCache.setCacheObject(request.getRemoteAddr(),verifyCode,1, TimeUnit.HOURS);
        return Result.ok(verifyCode);
    }

    /**
    * @description 获取验证码
    * @author Administrator
    * @date  20:17
    */
    @GetMapping("user/getVerifyCode")
    public void getVerifyCode(HttpServletRequest request, HttpServletResponse response){
        BufferedImage verifyCodeImage = VerifyCodeImageUtil.getVerifyCodeImage();
        String verifyCode = String.valueOf(VerifyCodeImageUtil.getVerifyCode());
        // 根据ip将验证码存入redis,设置过期时间1h
        redisCache.setCacheObject(request.getRemoteAddr(),verifyCode,1, TimeUnit.HOURS);
        try {
            //将验证码图片通过输出流做出响应
            ImageIO.write(verifyCodeImage,"JPEG",response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * @description 用户登录
    * @author Administrator
    * @date  13:38
    */
    @PostMapping("user/login")
    public Result login(@RequestBody LoginVo loginVo,HttpServletRequest request){
        // 验证码校验
        if ("".equals(loginVo.getCode()) || !loginVo.getCode().equalsIgnoreCase(redisCache.getCacheObject(request.getRemoteAddr()))){
            return new Result(201,"验证码错误！");
        }
        return userService.login(loginVo);
    }

    /**
    * @description 获取用户信息
    * @author Administrator
    * @date  17:11
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("user/getInfo")
    public Result getUserInfo(@RequestHeader String token){
        return userService.getUserInfo(token);
    }

    /**
    * @description 用户退出
    * @author Administrator
    * @date  13:38
    */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("user/logout")
    public Result logout(){
        return userService.logout();
    }

    /**
    * @description 重置密码
    * @author Administrator
    * @date  15:49
    */
    @PostMapping("user/resetPassword/{email}")
    public Result resetPassword(@PathVariable String email) throws MessagingException, UnsupportedEncodingException {
        return userService.resetPassword(email);
    }

    /**
     * ChatGPT
     */
    @PostMapping("chat")
    public Result chat(@RequestBody Map<String,String> map){
        String response = new ChatGPTUtil().getResponse(map.get("question"));
        return Result.ok(response);
    }
}
