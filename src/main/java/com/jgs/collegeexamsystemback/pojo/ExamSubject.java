package com.jgs.collegeexamsystemback.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description 考试科目实体类
 * @date 2023/7/18 0018 22:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("exam_subject")
public class ExamSubject implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 9L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 科目名称
    private String name;

    // 考试时长
    private Integer duration;

    // 考试班级
    @TableField(exist = false)
    private List<String> classes;

    // 考试人数
    private Integer students;
}
