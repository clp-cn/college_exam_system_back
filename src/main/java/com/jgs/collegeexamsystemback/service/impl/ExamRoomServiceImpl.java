package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.mapper.ExamRoomMapper;
import com.jgs.collegeexamsystemback.pojo.ExamRoom;
import com.jgs.collegeexamsystemback.service.ExamRoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamRoomServiceImpl
 * @date 2023/7/18 0018 22:53
 */
@Service
public class ExamRoomServiceImpl extends ServiceImpl<ExamRoomMapper, ExamRoom> implements ExamRoomService {

    // 获取所有教室
    @Override
    public List<ExamRoom> getAll() {
        return baseMapper.selectList(null);
    }

    // 根据条件分页查询考场列表
    @Override
    public Page<ExamRoom> getExamRoomPageList(Page<ExamRoom> page, Map<String,String> query) {
        QueryWrapper<ExamRoom> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("teach_building",query.get("teachBuilding"));
        return baseMapper.selectPage(page, queryWrapper);
    }
}
