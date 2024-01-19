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
 * @description 学院实体类
 * @date 2023/7/21 0021 14:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("college")
public class College implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 3L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 学院名称
    private String college;

    // 学院院长
    private String dean;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
