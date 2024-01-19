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
 * @description 专业班级实体类
 * @date 2023/7/18 0018 22:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("profession_class")
public class ProfessionClass implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 12L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 班级名称
    private String name;

    // 所属学院
    private String college;

    // 所属专业
    private String profession;

    // 所属年级
    private String grade;

    // 学生人数
    private Integer students;

    // 创建时间
    private Date createTime;

}
