package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.ExamSubject;
import com.jgs.collegeexamsystemback.pojo.ProfessionClass;
import com.jgs.collegeexamsystemback.vo.ExamSubjectVo;
import com.jgs.collegeexamsystemback.vo.UpdateSubjectVo;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamSubjectService
 * @date 2023/7/18 0018 22:50
 */
public interface ExamSubjectService extends IService<ExamSubject> {
    // 获取所有的考试科目
    List<ExamSubject> getAll();

    //
    List<ProfessionClass> getClassesByExamSubject(ExamSubject examSubject);

    // 获取科目分页列表
    Result getSubjectPageList(int pageNo,int pageSize);

    // 新增科目
    Result saveSubject(ExamSubjectVo examSubjectVo);

    // 更新科目信息
    Result updateSubject(UpdateSubjectVo updateSubjectVo);
}
