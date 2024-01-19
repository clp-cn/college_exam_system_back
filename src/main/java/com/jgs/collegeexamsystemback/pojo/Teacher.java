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
 * @description 监考老师实体类
 * @date 2023/7/18 0018 22:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("teacher")
public class Teacher implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 16L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 教工号
    private Integer no;

    // 姓名
    private String name;

    // 学院
    private String college;

    // 性别
    private String sex;

    // 电话
    private String phone;

    // 职位
    private String position;

    // 邮箱
    private String email;
}
