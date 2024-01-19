package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.ExamClass;
import com.jgs.collegeexamsystemback.vo.AddClassVo;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamClassService
 * @date 2023/8/29 0029 12:15
 */
public interface ExamClassService extends IService<ExamClass> {
    // 获取所有考试班级
    List<ExamClass> getAll();

    // 新增考试班级
    Result addClass(AddClassVo addClassVo);

    // 根据ids获取考试班级列表
    List<ExamClass> getClassesByIds(List<Long> classIds);

    // 删除班级
    Result deleteClass(Integer classId);

    // 批量删除考试班级
    Result deleteClasses(List<Integer> classIds);
}
