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
 * @description 考生实体类
 * @date 2023/7/18 0018 22:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("student")
public class Student implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 13L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 学号
    private Integer no;

    // 姓名
    private String name;

    // 学院
    private String college;

    // 专业
    private String profession;

    // 年级
    private String grade;

    // 班级
    private String className;

    @TableField(exist = false)
    // 公共班级（实际上不存在）
    private List<String> electiveClass;

    // 电话
    private String phone;

    // 性别
    private String sex;

}
