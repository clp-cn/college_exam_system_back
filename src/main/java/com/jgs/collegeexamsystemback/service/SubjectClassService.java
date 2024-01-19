package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.SubjectClass;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description SubjectClassService
 * @date 2023/7/19 0019 13:38
 */
public interface SubjectClassService extends IService<SubjectClass> {

    //根据科目id获取班级ids
    List<Long> getClassIdsBySubjectId(Long id);

    // 新增科目
    Result saveSubjectClass(List<String> classes, String name);

    // 删除科目
    Result deleteSubjectClass(Integer subjectId);

    // 批量删除科目
    Result deleteSubjectClasses(List<Integer> subjectIds);
}
