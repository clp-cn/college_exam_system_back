package com.jgs.collegeexamsystemback.vo;

import lombok.Data;

@Data
public class UpdateSubjectVo {
    private Long id;
    private String name;
    private Integer duration;
    private String className;
}
