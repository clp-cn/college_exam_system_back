package com.jgs.collegeexamsystemback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 * @version 1.0
 * @description 监考老师监考信息列表类
 * @date 2023/7/27 0027 14:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvigilatorExam {
    // 监考时间
    private String invigilateTime;

    // 监考科目
    private String invigilateSubject;

    // 监考地点
    private String invigilateVenue;

    // 监考教室
    private String invigilateRoom;

    // 监考时长
    private Integer invigilateDuration;

    // 监考状态(进行中、已结束）
    private String invigilateStatus;
}
