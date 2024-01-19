package com.jgs.collegeexamsystemback.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("student_class")
public class StudentClass implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 20L;

    // 学生与班级关联id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 学生id
    private Long studentId;

    // 专业班级id
    private Long professionClassId;

    // 考试班级id
    private Long examClassId;
}
