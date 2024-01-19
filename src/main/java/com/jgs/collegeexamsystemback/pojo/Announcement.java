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
 * @description 通知实体类
 * @date 2023/8/3 0003 19:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("announcement")
public class Announcement implements Serializable {

    @TableField(exist = false)
    private static final long serializableId = 2L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 通知内容
    private String announcement;

    // 创建时间
    private Date createTime;
}
