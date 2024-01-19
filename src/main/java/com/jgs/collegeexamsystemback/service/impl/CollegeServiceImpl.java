package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.mapper.CollegeMapper;
import com.jgs.collegeexamsystemback.pojo.College;
import com.jgs.collegeexamsystemback.service.CollegeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description CollegeServiceImpl
 * @date 2023/8/13 0013 11:39
 */
@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    // 根据搜索名称查询学院列表
    @Override
    public List<College> getCollegesBySearchName(String college) {
        QueryWrapper<College> wrapper = new QueryWrapper<>();
        wrapper.like("college",college);
        return baseMapper.selectList(wrapper);
    }

}
