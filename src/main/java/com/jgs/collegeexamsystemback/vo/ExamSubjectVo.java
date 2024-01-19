package com.jgs.collegeexamsystemback.vo;

import lombok.Data;


import java.io.Serializable;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamSubjectVo
 * @date 2023/7/22 0022 20:51
 */
@Data
public class ExamSubjectVo {
    private Long id;

    // 科目名称
    private String name;

    // 考试时长
    private Integer duration;

    // 考试班级
    private String classes;

    // 考试人数
    private Integer students;
}
