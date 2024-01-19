package com.jgs.collegeexamsystemback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 * @version 1.0
 * @description 学院人数类
 * @date 2023/7/29 0029 12:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollegeStudents {
    private String college;
    private Integer students;
}
