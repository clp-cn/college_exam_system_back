package com.jgs.collegeexamsystemback.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @TableName 用户实体类
 */
@TableName(value ="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 17L;
    /**
     * 用户id
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户电话号码
     */
    private String telephone;

    /**
     * 用户qq
     */
    private String qq;

    /**
    * 用户邮箱
    */
    private String qqEmail;

    /**
     * 用户性别
     */
    private Object gender;

    /**
     * 用户关联教师或学生号
     */
    private Integer teacherStudentNo;

    /**
     * 用户角色
     */
    private Object roles;

    /**
     * 用户头像
    */
    private String avatar;

    /**
    *  账号状态(0正常 1停用)
    */
    private Integer status;

    // 用户访问次数
    private Integer access;

    // 绑定的gitee用户名
    @TableField(value = "gitee")
    private String gitee;

    // 单端登录（0：未登录；1：已登录）
    private Integer singleLogin;
}
