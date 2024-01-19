package com.jgs.collegeexamsystemback.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Administrator
 * @version 1.0
 * @description 考试班级与考试科目关联实体类
 * @date 2023/7/19 0019 12:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("subject_class")
public class SubjectClass implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 15L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 考试科目id
    private Long subjectId;

    // 考试班级id
    private Long classId;
}
