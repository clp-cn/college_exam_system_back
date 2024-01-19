package com.jgs.collegeexamsystemback.filter;

import cn.hutool.core.util.ObjectUtil;
import com.jgs.collegeexamsystemback.dto.LoginUser;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
 * @description JWT认证过滤器
 * @date 2023/7/13 0013 14:18
 */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Resource
    private RedisCache redisCache;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)){
            // 放行
            filterChain.doFilter(request,response);
            return;
        }
        // 解析token
        Claims claims = null;
        try {
            claims = JwtUtil.parseJwt(token);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("token不合法");
        }
        // 从redis在获取用户信息
        String redisKey = "login_token:" + claims.getId();
        LoginUser loginUser = redisCache.getCacheObject(redisKey);
        if (ObjectUtil.isNull(loginUser)){
            throw new RuntimeException("用户未登录或登录已超时，请重新登录！");
        }
        // 存入SecurityContextHolder
        // 获取用户权限信息封装到authentication中
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(claims, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // 放行
        filterChain.doFilter(request,response);
    }
}
