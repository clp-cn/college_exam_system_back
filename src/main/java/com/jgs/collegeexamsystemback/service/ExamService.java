package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Exam;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamService
 * @date 2023/7/19 0019 13:16
 */
public interface ExamService extends IService<Exam> {
    void schedulerExams();

    // 获取考试安排分页列表
    Result getExamList(Integer pageNo,Integer pageSize);

    List<Exam> getExamsByClassName(String className);

    List<Exam> getExamsByInvigilator(Long teacherNo);

    // 删除所有的考试安排
    Result deleteAll();

    boolean isClash(int i);

    // 接收excel文件 批量插入考试安排数据
    Result saveExams(List<List<Object>> lists);

}
