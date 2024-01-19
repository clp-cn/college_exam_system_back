package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Student;
import com.jgs.collegeexamsystemback.vo.StudentQueryVo;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @description StudentService
 * @date 2023/7/18 0018 22:49
 */
public interface StudentService extends IService<Student> {

    // 获取学生分页列表
    Page<Student> getStudentPageList(Page<Student> page, StudentQueryVo studentQueryVo);

    // 获取学生考试信息
    Result getStudentExams(Long studentNo);

    // 添加学生
    Result saveStudent(Student student);

    // 删除学生
    Result deleteStudent(Long id);

    // 批量删除学生
    Result deleteStudents(List<Student> students);

    // 更新学生信息
    Result updateStudent(Student student);

    // 添加公共班级
    Result addElectiveClass(Map<String, String> map,Long studentId);

    // 移除公共班级
    Result removeElectiveClass(Long studentId,String className);

    // 接收excel文件 批量插入学生数据
    Result saveStudents(List<List<Object>> lists);

    // 删除所有学生
    Result deleteAll();
}
