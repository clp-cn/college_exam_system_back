package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.pojo.ProfessionClass;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ProfessionClassService
 * @date 2023/7/18 0018 23:28
 */
public interface ProfessionClassService extends IService<ProfessionClass> {

    // 获取所有的专业班级
    List<ProfessionClass> getAll();

    // 根据ids获取专业班级列表
    List<ProfessionClass> getClassesByIds(List<Long> classIds);
}
