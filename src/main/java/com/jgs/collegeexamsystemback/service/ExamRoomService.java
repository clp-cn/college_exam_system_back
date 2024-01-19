package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.pojo.ExamRoom;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamRoomService
 * @date 2023/7/18 0018 22:50
 */
public interface ExamRoomService extends IService<ExamRoom> {

    // 获取所有教室
    List<ExamRoom> getAll();

    // 获取教室分页列表
    Page<ExamRoom> getExamRoomPageList(Page<ExamRoom> page, Map<String,String> query);
}
