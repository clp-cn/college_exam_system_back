package com.jgs.collegeexamsystemback.vo;

import lombok.Data;

/**
 * @author Administrator
 * @version 1.0
 * @description UpdateUserVo
 * @date 2023/7/21 0021 10:41
 */
@Data
public class UpdateUserVo {
    private Long userId;
    private String username;
    private String nickname;
    private String password;
    private String status;
    private String gender;
    private String roles;
}
