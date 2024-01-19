package com.jgs.collegeexamsystemback.vo;

import lombok.Data;

/**
 * @author Administrator
 * @version 1.0
 * @description 用户登录类
 * @date 2023/6/30 0030 18:31
 */
@Data
public class LoginVo {
    private String username;
    private String password;
    private String code;
}
