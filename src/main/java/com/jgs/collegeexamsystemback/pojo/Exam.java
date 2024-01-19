package com.jgs.collegeexamsystemback.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Administrator
 * @version 1.0
 * @description 考试安排实体类
 * @date 2023/7/18 0018 22:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("exam")
public class Exam implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 4L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 考场教学楼
    private String teachBuilding;

    // 考场
    private String classRoom;

    // 监考老师教工号
    private Integer invigilator;

    // 监考老师姓名
    @TableField(exist = false)
    private String invigilatorName;

    // 考试科目
    private String examSubject;

    // 考试开始时间
    private Date examStart;

    // 考试结束时间
    private Date examEnd;

    // 考试班级
    private String examClass;
}
