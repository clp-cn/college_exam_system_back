package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.mapper.ProfessionClassMapper;
import com.jgs.collegeexamsystemback.pojo.ProfessionClass;
import com.jgs.collegeexamsystemback.service.ProfessionClassService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ProfessionClassServiceImpl
 * @date 2023/7/18 0018 23:29
 */
@Service
public class ProfessionClassServiceImpl extends ServiceImpl<ProfessionClassMapper, ProfessionClass> implements ProfessionClassService {

    // 获取所有的专业班级
    @Override
    public List<ProfessionClass> getAll() {
        return baseMapper.selectList(null);
    }

    // 根据ids获取classes
    @Override
    public List<ProfessionClass> getClassesByIds(List<Long> classIds) {
        return baseMapper.selectBatchIds(classIds);
    }
}
