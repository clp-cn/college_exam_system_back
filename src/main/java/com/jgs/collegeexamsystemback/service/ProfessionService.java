package com.jgs.collegeexamsystemback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Profession;

/**
 * @author Administrator
 * @version 1.0
 * @description ProfessionService
 * @date 2023/8/13 0013 11:44
 */
public interface ProfessionService extends IService<Profession> {
    // 根据学院获取专业列表
    Result getProfessionsByCollege(String college);
}
