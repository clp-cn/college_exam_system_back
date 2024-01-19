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
 * @description 考试班级实体类
 * @date 2023/8/29 0029 12:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("exam_class")
public class ExamClass implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 6L;

    // 考试班级id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 考试班级名称
    private String name;

    // 所属学院
    private String college;

    // 所属年级
    private String grade;

    // 班级类型
    private String type;

    // 班级人数
    private Integer students;

    // 创建时间
    private Date createTime;
}
