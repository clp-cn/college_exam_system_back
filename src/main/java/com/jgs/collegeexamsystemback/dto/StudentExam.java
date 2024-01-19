package com.jgs.collegeexamsystemback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 * @version 1.0
 * @description 学生考试安排
 * @date 2023/7/27 0027 13:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentExam {
    // 考试时间
    private String examTime;

    // 考试科目
    private String examSubject;

    // 考试地点
    private String examVenue;

    // 考试教室
    private String examRoom;

    // 考试时长
    private Integer examDuration;

    // 座位号
    private Integer seatNumber;

    // 考试状态（进行中、已结束）
    private String examStatus;
}
