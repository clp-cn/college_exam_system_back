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
 * @description 考场实体类
 * @date 2023/7/18 0018 22:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("exam_room")
public class ExamRoom implements Serializable {

    @TableField(exist = false)
    private static final long serializableId = 8L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 教学楼
    private String teachBuilding;

    // 教室名称
    private String name;

    // 教室容量
    private Integer capacity;

}
