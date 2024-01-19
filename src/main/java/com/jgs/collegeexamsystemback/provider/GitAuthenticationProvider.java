package com.jgs.collegeexamsystemback.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.jgs.collegeexamsystemback.mapper.UserMapper;
import com.jgs.collegeexamsystemback.pojo.User;
import com.jgs.collegeexamsystemback.token.GitAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @version 1.0
 * @description GitAuthenticationProvider
 * @date 2023/8/4 0004 21:39
 */
@Component
public class GitAuthenticationProvider implements AuthenticationProvider {
    @Resource
    private UserMapper userMapper;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        GitAuthenticationToken token = (GitAuthenticationToken) authentication;
        if (ObjectUtils.isEmpty(token.getGitee())){
            throw new BadCredentialsException("Gitee账户为空！");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("gitee", token.getGitee()));
        if (user == null){
            throw new BadCredentialsException("该Gitee账户未绑定用户！");
        }
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GitAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
