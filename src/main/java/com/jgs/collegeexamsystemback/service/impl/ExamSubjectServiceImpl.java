package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.ExamSubjectMapper;
import com.jgs.collegeexamsystemback.pojo.ExamClass;
import com.jgs.collegeexamsystemback.pojo.ExamSubject;
import com.jgs.collegeexamsystemback.pojo.ProfessionClass;
import com.jgs.collegeexamsystemback.pojo.SubjectClass;
import com.jgs.collegeexamsystemback.service.ExamClassService;
import com.jgs.collegeexamsystemback.service.ExamSubjectService;
import com.jgs.collegeexamsystemback.service.ProfessionClassService;
import com.jgs.collegeexamsystemback.service.SubjectClassService;
import com.jgs.collegeexamsystemback.vo.ExamSubjectVo;
import com.jgs.collegeexamsystemback.vo.UpdateSubjectVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamSubjectServiceImpl
 * @date 2023/7/18 0018 22:54
 */
@Service
@Transactional
public class ExamSubjectServiceImpl extends ServiceImpl<ExamSubjectMapper, ExamSubject> implements ExamSubjectService {
    @Resource
    private SubjectClassService subjectClassService;
    @Resource
    private ProfessionClassService professionClassService;
    @Resource
    private ExamClassService examClassService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ExamSubjectService examSubjectService;

    // 获取所有科目
    @Override
    public List<ExamSubject> getAll() {
        return baseMapper.selectList(null);
    }

    // 根据科目查出所有的班级
    @Override
    public List<ProfessionClass> getClassesByExamSubject(ExamSubject examSubject) {
        List<Long> classIds = subjectClassService.getClassIdsBySubjectId(examSubject.getId());
        return professionClassService.getClassesByIds(classIds);
    }

    // 获取考试科目分页列表
    @Override
    public Result getSubjectPageList(int pageNo,int pageSize){
        String redisKey = pageNo + "_" + pageSize;
        Page<ExamSubject> examSubjectPage = redisCache.getCacheMapValue("subject", redisKey);
        if (examSubjectPage == null){
            Page<ExamSubject> selectPage = baseMapper.selectPage(new Page<>(pageNo, pageSize), null);
            List<ExamSubject> records = selectPage.getRecords();
            records.forEach(r -> {
                // 根据科目id查出所有班级id
                List<SubjectClass> subjectClasses = subjectClassService.getBaseMapper().selectList(new QueryWrapper<SubjectClass>().eq("subject_id", r.getId()));
                List<Long> ids = new ArrayList<>();
                subjectClasses.forEach(sc -> ids.add(sc.getClassId()));
                // 根据班级id查询班级名称
                List<String> classes = new ArrayList<>();
                List<ExamClass> examClasses = examClassService.getClassesByIds(ids);
                examClasses.forEach(examClass -> classes.add(examClass.getName()));
                r.setClasses(classes);
            });
            selectPage.setRecords(records);
            redisCache.setCacheMapValue("subject",redisKey,selectPage);
            return Result.ok(selectPage);
        }
        return Result.ok(examSubjectPage);
    }

    // 新增考试科目
    @Override
    public Result saveSubject(ExamSubjectVo examSubjectVo) {
        String[] split = examSubjectVo.getClasses().split("、");
        List<String> classes = Arrays.asList(split);
        ExamSubject examSubject = new ExamSubject();
        examSubject.setDuration(examSubjectVo.getDuration());
        examSubject.setName(examSubjectVo.getName());
        examSubject.setStudents(examSubjectVo.getStudents());
        int result = baseMapper.insert(examSubject);
        if (result > 0){
            redisCache.deleteObject("subject");
            return subjectClassService.saveSubjectClass(classes,examSubject.getName());
        }
        return new Result(201,"添加失败，请重试！");
    }

    // 更新科目信息
    @Override
    public Result updateSubject(UpdateSubjectVo updateSubjectVo) {
        if (updateSubjectVo != null){
            ExamSubject examSubject = examSubjectService.getById(updateSubjectVo.getId());
            examSubject.setDuration(updateSubjectVo.getDuration());
            String[] split = updateSubjectVo.getClassName().split("、");
            List<String> classNames = Arrays.asList(split);
            int total = 0;
            for (String className: classNames){
                ExamClass examClass = examClassService.getOne(new QueryWrapper<ExamClass>().eq("name", className));
                if (examClass == null){
                    return Result.fail("修改失败，班级不存在！");
                }
                SubjectClass subjectClass = new SubjectClass();
                subjectClass.setSubjectId(updateSubjectVo.getId());
                subjectClass.setClassId(examClass.getId());
//                boolean remove = subjectClassService.remove(new QueryWrapper<SubjectClass>().eq("subject_id", updateSubjectVo.getId()));
//                if (!remove){
//                    return Result.fail("修改失败！");
//                }
                boolean save = subjectClassService.save(subjectClass);
                if (!save){
                    return new Result(201,"修改失败！");
                }
                total += examClass.getStudents();
            }
            examSubject.setClasses(classNames);
            examSubject.setStudents(total);
            boolean result = examSubjectService.updateById(examSubject);
            if (!result){
                return new Result(201,"修改失败！");
            }
        }
        redisCache.deleteObject("subject");
        return new Result(200,"修改成功！");
    }
}
