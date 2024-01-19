package com.jgs.collegeexamsystemback.service.impl;

import com.alibaba.fastjson.JSON;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.util.WebUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @description 自定义SpringSecurity失败处理
 * @date 2023/7/13 0013 17:29
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Result result = new Result(HttpStatus.FORBIDDEN.value(), "用户权限不足");
        String json = JSON.toJSONString(result);
        WebUtil.renderString(response,json);
    }
}
