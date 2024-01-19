package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.ProfessionMapper;
import com.jgs.collegeexamsystemback.pojo.College;
import com.jgs.collegeexamsystemback.pojo.Profession;
import com.jgs.collegeexamsystemback.service.CollegeService;
import com.jgs.collegeexamsystemback.service.ProfessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ProfessionServiceImpl
 * @date 2023/8/13 0013 11:45
 */
@Service
@Transactional
public class ProfessionServiceImpl extends ServiceImpl<ProfessionMapper, Profession> implements ProfessionService {
    @Resource
    private CollegeService collegeService;

    // 根据学院获取专业列表
    @Override
    public Result getProfessionsByCollege(String college) {
        // 根据搜索名称获取学院列表
        List<College> collegeList = collegeService.getCollegesBySearchName(college);
        // 获取所有的专业名称
        List<String> professions = new ArrayList<>();
        collegeList.forEach(c -> {
            QueryWrapper<Profession> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("college",c.getCollege());
            List<Profession> professionList = baseMapper.selectList(queryWrapper);
            professionList.forEach(profession -> professions.add(profession.getProfession()));
        });
        return Result.ok(professions);
    }
}
