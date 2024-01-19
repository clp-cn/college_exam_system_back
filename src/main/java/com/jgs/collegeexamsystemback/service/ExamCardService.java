package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.ExamCard;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamCardService
 * @date 2023/8/14 0014 11:26
 */
public interface ExamCardService extends IService<ExamCard> {

    // 接收excel文件 批量插入准考证号数据
    Result saveExamCards(List<List<Object>> lists);

    // 删除所有的准考证号
    Result deleteExamCardAll();
}
