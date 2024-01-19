package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.LoginUser;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.MenuMapper;
import com.jgs.collegeexamsystemback.pojo.Menu;
import com.jgs.collegeexamsystemback.service.MenuService;
import com.jgs.collegeexamsystemback.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description MenuServiceImpl
 * @date 2023/7/7 0007 15:49
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Resource
    private RedisCache redisCache;

    /**
    * 根据用户权限查询菜单列表
    * @author Administrator
    * @date  18:03
    */
    @Override
    public Result getMenusByPerms(String token) {
        // 解析token
        Claims claims = JwtUtil.parseJwt(token);
        // 从redis中获取用户信息
        String redisKey = "login_token:" + claims.getId();
        LoginUser loginUser = redisCache.getCacheObject(redisKey);
        String perms = (String) loginUser.getUser().getRoles();
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("perms",perms);
        List<Menu> menus = baseMapper.selectList(queryWrapper);
        if (menus == null){
            return Result.ok(null);
        }
        return Result.ok(menus);
    }
}
