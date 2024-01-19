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
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description 菜单实体类
 * @date 2023/7/7 0007 15:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("menu")
public class Menu implements Serializable {
    @TableField(exist = false)
    private static final long serializableId = 10L;

    // id
    @TableId(type = IdType.AUTO)
    private Long id;

    // 菜单名称
    private String name;

    // 菜单路径
    private String path;

    // 操作权限
    private String perms;

}
