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
 * @description 专业实体类
 * @date 2023/8/13 0013 11:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("profession")
public class Profession implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 11L;

    // id
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 专业名称
    private String profession;

    // 所属学院
    private String college;

    // 创建时间
    private Date createTime;
}
