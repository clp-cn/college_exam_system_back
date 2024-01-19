package com.jgs.collegeexamsystemback.handler;

import com.jgs.collegeexamsystemback.util.WebUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @description 认证失败处理器
 * @date 2023/7/13 0013 17:15
 */
@Component
public class SGFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        WebUtil.renderString(response,"认证失败,请重新登录！");
    }
}
