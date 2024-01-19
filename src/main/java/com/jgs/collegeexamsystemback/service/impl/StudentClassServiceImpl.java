package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.mapper.StudentClassMapper;
import com.jgs.collegeexamsystemback.pojo.StudentClass;
import com.jgs.collegeexamsystemback.service.StudentClassService;
import org.springframework.stereotype.Service;

@Service
public class StudentClassServiceImpl extends ServiceImpl<StudentClassMapper, StudentClass> implements StudentClassService {
}
