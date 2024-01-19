package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.mapper.ExamDateMapper;
import com.jgs.collegeexamsystemback.pojo.ExamDate;
import com.jgs.collegeexamsystemback.service.ExamDateService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamDateServiceImpl
 * @date 2023/7/19 0019 11:15
 */
@Service
public class ExamDateServiceImpl extends ServiceImpl<ExamDateMapper, ExamDate> implements ExamDateService {

    // 获取所有的考试时间
    @Override
    public List<ExamDate> getAll() {
        return baseMapper.selectList(null);
    }
}
