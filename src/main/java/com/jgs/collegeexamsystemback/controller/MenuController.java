package com.jgs.collegeexamsystemback.controller;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.service.MenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
/**
 * @author Administrator
 * @version 1.0
 * @description 菜单资源控制
 * @date 2023/7/7 0007 15:51
 */
@RestController
@RequestMapping("menu")
public class MenuController {
    @Resource
    private MenuService menuService;

    /**
     * 获取菜单
     */
    @PreAuthorize("hasAnyAuthority('管理员','学生','教师')")
    @GetMapping("getMenus")
    public Result getMenuByPerms(@RequestHeader String token){
        return menuService.getMenusByPerms(token);
    }
}
