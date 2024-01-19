package com.jgs.collegeexamsystemback.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.mapper.UserMapper;
import com.jgs.collegeexamsystemback.pojo.User;
import com.jgs.collegeexamsystemback.token.SmsAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @version 1.0
 * @description SmsAuthenticationProvider
 * @date 2023/8/4 0004 13:53
 */
@Component
public class SmsAuthenticationProvider implements AuthenticationProvider {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisCache redisCache;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
        if (ObjectUtils.isEmpty(token.getPhone()) || ObjectUtils.isEmpty(token.getCode())){
            throw new BadCredentialsException("手机号或验证码为空！");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("telephone", token.getPhone()));
        if (user == null){
            throw new BadCredentialsException("该手机号暂未绑定用户！");
        }else {
            if (!redisCache.getCacheObject(token.getPhone()).equals(token.getCode())){
                throw new BadCredentialsException("验证码错误！");
            }
        }
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
