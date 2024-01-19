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
 * @description 准考证实体类
 * @date 2023/8/14 0014 11:22
**/
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("exam_card")
public class ExamCard implements Serializable {

    @TableField(exist = false)
    private static final long serializableId = 5L;

    // id
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 考号
    private Integer no;

    // 姓名
    private String name;

    // 性别
    private String sex;

    // 班级
    private String className;

}
