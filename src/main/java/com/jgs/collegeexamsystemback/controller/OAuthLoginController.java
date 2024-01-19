package com.jgs.collegeexamsystemback.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @version 1.0
 * @description 第三方登录控制类
 * @date 2023/7/31 0031 16:16
 */
@RestController
@RequestMapping("oauth")
public class OAuthLoginController {
    private static final String clientId = "cb8c2d0064eb83a3cbddc582d761a60971fb073d3be8202dc8d95aecfcd21cbd";
    private static final String clientSecret = "3eb156c10228d3b18a061cdc8830369d01718d6cd58252d17ab4ff6bad1cd778";
    private static final String redirectUri = "http://localhost:10010/oauth/callback";
    private static final String tokenServer = "https://gitee.com";
    private static final String userUrl = tokenServer + "/api/v5/user";
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private UserService userService;
    @Resource
    private RedisCache redisCache;

    /**
    * @description 手机号登录
    * @author Administrator
    * @date  17:44
    */
    @GetMapping("loginPhone")
    public Result loginPhone(@RequestParam String phone,@RequestParam String code){
        return userService.loginPhone(phone,code);
    }

    /**
    * @description 短信发送
    * @author Administrator
    * @date  17:45
    */
    @GetMapping("sendSms/{phone}")
    public Result sendSms(@PathVariable String phone){
        String verifyCode = String.valueOf((int)((Math.random() * 9 + 1) * 1000));
        redisCache.setCacheObject(phone,verifyCode,5, TimeUnit.MINUTES);
        return Result.ok(verifyCode);
    }

    /**
    * @description gitee登录
    * @author Administrator
    * @date  17:46
    */
    @GetMapping("loginGit")
    public Result loginGit(){
        String url = tokenServer + "/oauth/authorize?client_id=%s" +
                "&redirect_uri=%s&response_type=code";
        url = String.format(url,clientId,redirectUri);
        return Result.ok(url);
    }

    /**
    * @description gitee登录
    * @author Administrator
    * @date  17:45
    */
    @GetMapping("getUserByGit/{gitee}")
    public Result getUserByGit(@PathVariable String gitee){
        return userService.loginGit(gitee);
    }

    /**
     * 回调接口
     * @throws IOException
     */
    @GetMapping("callback")
    public void callback(String code, HttpServletResponse response) throws IOException {
//         获取令牌参数
//         authorization_code 授权码模式
//         password           密码模式
        String body = "grant_type=authorization_code&code=" + code +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&client_secret=" + clientSecret;
        String s = "";
        Map<String,Object> params = MapUtil.<String,Object>builder().put("grant_type","authorization_code")
                .put("code",code)
                .put("client_id",clientId)
                .put("redirect_uri",redirectUri)
                .put("client_secret",clientSecret)
                .build();
        s = HttpUtil.post(tokenServer + "/oauth/token",params);
        // 使用ObjectMapper的方式去解析返回的内容
        HashMap hashMap = objectMapper.readValue(s, HashMap.class);
        String access_token = (String) hashMap.get("access_token");
        // 获取用户信息需要携带token
        String s2 = HttpUtil.get(userUrl + "?access_token=" + access_token);
        HashMap hashMap1 = objectMapper.readValue(s2, HashMap.class);
        String name = (String) hashMap1.get("name");
        response.sendRedirect("http://localhost:8080/#/gitee?name=" + name);
    }

}
