package com.jgs.collegeexamsystemback.vo;

import lombok.Data;

/**
 * @author Administrator
 * @version 1.0
 * @description AddClassVo
 * @date 2023/8/29 0029 13:25
 */
@Data
public class AddClassVo {
    private String name;

    private String college;

    private String profession;

    private String grade;

    private String type;

    private Integer students;
}
