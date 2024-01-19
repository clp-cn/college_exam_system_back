package com.jgs.collegeexamsystemback.vo;

import lombok.Data;

/**
 * @author Administrator
 * @version 1.0
 * @description UpdatePasswordVo
 * @date 2023/7/25 0025 14:04
 */
@Data
public class UpdatePasswordVo {
    private Long userId;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
