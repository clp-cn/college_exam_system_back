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
 * @description 考试日期实体类
 * @date 2023/7/19 0019 11:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("exam_date")
public class ExamDate implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 7L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 考试时间
    private Date time;

}
