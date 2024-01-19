package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.ExamClassMapper;
import com.jgs.collegeexamsystemback.pojo.ExamClass;
import com.jgs.collegeexamsystemback.pojo.ProfessionClass;
import com.jgs.collegeexamsystemback.service.ExamClassService;
import com.jgs.collegeexamsystemback.service.ProfessionClassService;
import com.jgs.collegeexamsystemback.vo.AddClassVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamClassServiceImpl
 * @date 2023/8/29 0029 13:04
 */
@Service
@Transactional
public class ExamClassServiceImpl extends ServiceImpl<ExamClassMapper, ExamClass> implements ExamClassService {
    @Resource
    private RedisCache redisCache;
    @Resource
    private ProfessionClassService professionClassService;
    @Resource
    private ExamClassService examClassService;

    // 获取所有考试班级
    @Override
    public List<ExamClass> getAll() {
        return baseMapper.selectList(null);
    }

    // 新增考试班级
    @Override
    public Result addClass(AddClassVo addClassVo) {
        if (addClassVo != null){
            ExamClass examClass = new ExamClass();
            examClass.setName(addClassVo.getName());
            examClass.setCollege(addClassVo.getCollege());
            examClass.setGrade(addClassVo.getGrade());
            examClass.setStudents(addClassVo.getStudents());
            examClass.setType(addClassVo.getType());
            examClass.setCreateTime(new Date());
            // 根据班级名称查询考试班级表，如果存在，不做添加操作
            ExamClass selectExamClass = examClassService.getOne(new QueryWrapper<ExamClass>().eq("name", addClassVo.getName()));
            if (selectExamClass != null){
                return new Result(201,"添加失败，班级重复！");
            }
            if (addClassVo.getType().equals("专业班级")){
                ProfessionClass professionClass = new ProfessionClass();
                professionClass.setName(addClassVo.getName());
                professionClass.setCollege(addClassVo.getCollege());
                professionClass.setProfession(addClassVo.getProfession());
                professionClass.setGrade(addClassVo.getGrade());
                professionClass.setStudents(addClassVo.getStudents());
                professionClass.setCreateTime(new Date());
                boolean save = professionClassService.save(professionClass);
                int save1 = baseMapper.insert(examClass);
                if (save && save1 > 0){
                    redisCache.deleteObject("classes");
                    return new Result(200,"添加成功！");
                }
            }else {
                int save = baseMapper.insert(examClass);
                if (save > 0){
                    redisCache.deleteObject("classes");
                    return new Result(200,"添加成功！");
                }
            }
        }
        return new Result(201,"添加失败！");
    }

    // 根据ids获取考试班级列表
    @Override
    public List<ExamClass> getClassesByIds(List<Long> classIds) {
        return baseMapper.selectBatchIds(classIds);
    }

    // 删除考试班级
    @Override
    public Result deleteClass(Integer classId) {
        if (classId != null){
            ExamClass examClass = examClassService.getById(classId);
            if (examClass != null){
                boolean result = examClassService.removeById(classId);
                ProfessionClass professionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", examClass.getName()));
                if (professionClass == null && result){
                    redisCache.deleteObject("classes");
                    return Result.ok();
                }
                if (professionClass != null && result){
                    boolean result1 = professionClassService.removeById(professionClass);
                    if (result1){
                        redisCache.deleteObject("classes");
                        return Result.ok();
                    }
                }
            }
        }
        return Result.fail();
    }

    // 批量删除考试班级
    @Override
    public Result deleteClasses(List<Integer> classIds) {
        if (classIds == null || classIds.isEmpty()){
            return new Result(201,"删除失败！");
        }
        List<ExamClass> examClasses = baseMapper.selectBatchIds(classIds);
        List<String> examClassNames = new ArrayList<>();
        int removeExamClass = baseMapper.deleteBatchIds(classIds);
        if (removeExamClass > 0){
            examClasses.forEach(examClass -> examClassNames.add(examClass.getName()));
            List<String> professionClassNames = new ArrayList<>();
            examClassNames.forEach(examClassName -> {
                ProfessionClass professionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", examClassName));
                if (professionClass != null){
                    professionClassNames.add(professionClass.getName());
                }
            });
            if (professionClassNames.isEmpty()){
                redisCache.deleteObject("classes");
                return new Result(200,"删除成功！");
            }
            boolean removeProfessionClass = professionClassService.remove(new QueryWrapper<ProfessionClass>().in("name", professionClassNames));
            if (removeProfessionClass){
                redisCache.deleteObject("classes");
                return new Result(200,"删除成功！");
            }
        }
        return new Result(201,"删除失败！");
    }
}
