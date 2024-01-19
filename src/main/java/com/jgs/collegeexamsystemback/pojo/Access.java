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
 * @description 用户访问实体类
 * @date 2023/8/3 0003 20:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("access")
public class Access implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 1L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 访问日期
    private String accessDate;

    // 访问次数
    private Integer number;
}
