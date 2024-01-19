package com.jgs.collegeexamsystemback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jgs.collegeexamsystemback.pojo.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Administrator
 * @version 1.0
 * @description StudentMapper
 * @date 2023/7/18 0018 22:46
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
