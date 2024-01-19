package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Teacher;
import com.jgs.collegeexamsystemback.vo.InvigilatorVo;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description TeacherService
 * @date 2023/7/18 0018 22:49
 */
public interface TeacherService extends IService<Teacher> {

    // 获取所有教师
    List<Teacher> getAll();

    // 获取监考老师分页列表
    Page<Teacher> getInvigilatorPageList(Page<Teacher> page, InvigilatorVo invigilatorVo);

    // 绑定教师
    Result bindTeacher(Long userId, Long teacherNo);

    // 获取老师的监考信息
    Result getTeacherExams(Long teacherNo);

    // 导入excel文件 批量插入教师数据
    Result saveTeachers(List<List<Object>> lists);

    // 删除所有的老师
    Result deleteAll();
}
