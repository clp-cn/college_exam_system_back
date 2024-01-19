package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.SubjectClassMapper;
import com.jgs.collegeexamsystemback.pojo.ExamClass;
import com.jgs.collegeexamsystemback.pojo.ExamSubject;
import com.jgs.collegeexamsystemback.pojo.SubjectClass;
import com.jgs.collegeexamsystemback.service.ExamClassService;
import com.jgs.collegeexamsystemback.service.ExamSubjectService;
import com.jgs.collegeexamsystemback.service.SubjectClassService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description SubjectClassServiceImpl
 * @date 2023/7/19 0019 13:38
 */
@Service
@Transactional
public class SubjectClassServiceImpl extends ServiceImpl<SubjectClassMapper, SubjectClass> implements SubjectClassService {
    @Resource
    private ExamSubjectService examSubjectService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ExamClassService examClassService;

    // 根据科目id获取班级ids
    @Override
    public List<Long> getClassIdsBySubjectId(Long id) {
        List<SubjectClass> subjectClasses = baseMapper.selectList(new QueryWrapper<SubjectClass>().eq("subject_id", id));
        ArrayList<Long> classIds = new ArrayList<>();
        for (SubjectClass s:subjectClasses) {
            classIds.add(s.getClassId());
        }
        return classIds;
    }

    // 新增科目
    @Override
    public Result saveSubjectClass(List<String> classes, String name) {
        for (String c:classes) {
            ExamClass examClass = examClassService.getOne(new QueryWrapper<ExamClass>().eq("name", c));
            if (examClass == null){
                examSubjectService.remove(new QueryWrapper<ExamSubject>().eq("name",name));
                return new Result(201,"考试班级不存在，请输入正确的考试班级！");
            }
            ExamSubject es = examSubjectService.getOne(new QueryWrapper<ExamSubject>().eq("name", name));
            SubjectClass subjectClass = new SubjectClass();
            subjectClass.setSubjectId(es.getId());
            subjectClass.setClassId(examClass.getId());
            int insert = baseMapper.insert(subjectClass);
            if (insert > 0){
                redisCache.deleteObject("subject");
                return Result.ok("添加成功!");
            }
        }
        return Result.fail();
    }

    // 删除科目
    @Override
    public Result deleteSubjectClass(Integer subjectId) {
        if (subjectId != null){
            List<SubjectClass> subjectClasses = baseMapper.selectList(new QueryWrapper<SubjectClass>().eq("subject_id", subjectId));
            if (subjectClasses == null || subjectClasses.isEmpty()){
                return new Result(201,"删除失败!");
            }
            int deleteSubjectClass = baseMapper.delete(new QueryWrapper<SubjectClass>().eq("subject_id", subjectId));
            boolean deleteExamSubject = examSubjectService.removeById(subjectId);
            if (deleteSubjectClass > 0 && deleteExamSubject){
                redisCache.deleteObject("subject");
                return new Result(200,"删除成功!");
            }
        }
        return new Result(201,"删除失败!");
    }

    // 删除科目列表
    @Override
    public Result deleteSubjectClasses(List<Integer> subjectIds) {
        if (subjectIds != null && !subjectIds.isEmpty()){
            List<Long> subjectClassIds = new ArrayList<>();
            subjectIds.forEach(subjectId -> {
                List<SubjectClass> subjectClasses = baseMapper.selectList(new QueryWrapper<SubjectClass>().eq("subject_id", subjectId));
                subjectClasses.forEach(subjectClass -> {
                    subjectClassIds.add(subjectClass.getId());
                });
            });
            int deleteSubjectClasses = baseMapper.deleteBatchIds(subjectClassIds);
            boolean deleteExamSubjects = examSubjectService.removeBatchByIds(subjectIds);
            if (deleteSubjectClasses > 0 && deleteExamSubjects){
                redisCache.deleteObject("subject");
                return Result.ok("删除成功!");
            }
        }
        return Result.fail("删除失败!");
    }
}
