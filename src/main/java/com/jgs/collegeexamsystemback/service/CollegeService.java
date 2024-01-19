package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.pojo.College;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description CollegeService
 * @date 2023/8/13 0013 11:38
 */
public interface CollegeService extends IService<College> {

    // 根据搜索名称获取学院列表
    List<College> getCollegesBySearchName(String college);

}
