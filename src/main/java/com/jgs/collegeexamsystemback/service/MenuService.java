package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Menu;

/**
 * @author Administrator
 * @version 1.0
 * @description MenuService
 * @date 2023/7/7 0007 15:48
 */
public interface MenuService extends IService<Menu> {

    // 获取菜单
    Result getMenusByPerms(String token);
}
