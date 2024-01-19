package com.jgs.collegeexamsystemback.service.impl;

import com.alibaba.fastjson.JSON;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.util.WebUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @description 自定义SpringSecurity认证失败处理
 * @date 2023/7/13 0013 17:31
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String message = authException.getMessage();
        Result result = new Result(HttpStatus.UNAUTHORIZED.value(), message);
        String json = JSON.toJSONString(result);
        WebUtil.renderString(response,json);
    }

}
