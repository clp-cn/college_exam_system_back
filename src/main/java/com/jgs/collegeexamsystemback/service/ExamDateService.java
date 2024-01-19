package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.pojo.ExamDate;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamDateService
 * @date 2023/7/19 0019 11:15
 */
public interface ExamDateService extends IService<ExamDate> {

    // 获取所有的考试时间
    List<ExamDate> getAll();
}
