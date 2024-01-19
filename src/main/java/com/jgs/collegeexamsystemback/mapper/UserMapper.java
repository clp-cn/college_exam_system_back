package com.jgs.collegeexamsystemback.mapper;

import com.jgs.collegeexamsystemback.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashSet;

/**
* @author Administrator
* @description 针对表【user】的数据库操作Mapper
* @createDate 2023-06-30 13:00:21
* @Entity generator.pojo.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    HashSet<String> selectPermsByUserId(Long userId);
}




