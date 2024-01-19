package com.jgs.collegeexamsystemback.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @description SmsAuthenticationFilter
 * @date 2023/8/4 0004 17:49
 */
@Component
public class SmsAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private RedisCache redisCache;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (StringUtils.equals("/oauth/loginPhone",request.getRequestURI()) &&  request.getMethod().equalsIgnoreCase("get")){
            String phone = request.getParameter("phone");
            String code = request.getParameter("code");
            if (!redisCache.getCacheObject(phone).equals(code)){
                throw new BadCredentialsException("验证码错误！");
            }
        }
        filterChain.doFilter(request,response);
    }
}
