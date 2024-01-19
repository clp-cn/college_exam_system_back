package com.jgs.collegeexamsystemback.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jgs.collegeexamsystemback.dto.LoginUser;
import com.jgs.collegeexamsystemback.mapper.UserMapper;
import com.jgs.collegeexamsystemback.pojo.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Administrator
 * @version 1.0
 * @description UserDetailsImpl
 * @date 2023/7/13 0013 14:00
 */
@Service
public class UserDetailsImpl implements UserDetailsService {
    @Resource
    private UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        // 如果没有查询到用户就抛出异常
        if (ObjectUtil.isNull(user)){
            throw new RuntimeException("用户名或密码错误！");
        }
        // 查询对应的权限信息
        HashSet<String> perms = userMapper.selectPermsByUserId(user.getUserId());
        return new LoginUser(user,perms);
    }
}
