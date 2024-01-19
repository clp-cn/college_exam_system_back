package com.jgs.collegeexamsystemback.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.InvigilatorExam;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.TeacherMapper;
import com.jgs.collegeexamsystemback.pojo.Exam;
import com.jgs.collegeexamsystemback.pojo.Teacher;
import com.jgs.collegeexamsystemback.pojo.User;
import com.jgs.collegeexamsystemback.service.ExamService;
import com.jgs.collegeexamsystemback.service.TeacherService;
import com.jgs.collegeexamsystemback.service.UserService;
import com.jgs.collegeexamsystemback.vo.InvigilatorVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description TeacherServiceImpl
 * @date 2023/7/18 0018 22:51
 */
@Service
@Transactional
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
    @Resource
    private UserService userService;
    @Resource
    private ExamService examService;
    @Resource
    private RedisCache redisCache;

    // 获取所有教师
    @Override
    public List<Teacher> getAll() {
        return baseMapper.selectList(null);
    }

    // 根据条件分页查询监考老师列表
    @Override
    public Page<Teacher> getInvigilatorPageList(Page<Teacher> page, InvigilatorVo invigilatorVo) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name",invigilatorVo.getName())
                .like("college",invigilatorVo.getCollege())
                .like("position",invigilatorVo.getPosition());
        return baseMapper.selectPage(page, queryWrapper);
    }

    // 绑定教师
    @Override
    public Result bindTeacher(Long userId, Long teacherNo) {
        if (userId != null && teacherNo != null){
            User user = userService.getById(userId);
            Teacher teacher = baseMapper.selectOne(new QueryWrapper<Teacher>().eq("no", teacherNo));
            if (teacher == null){
                return new Result(201,"不存在该教师！");
            }
            // 判断该教师是否已被绑定
            User isExist = userService.getOne(new QueryWrapper<User>().eq("teacher_student_no", teacher.getNo()));
            if (!ObjectUtil.isNull(isExist)){
                return new Result(201,"绑定失败，该教师已被绑定！");
            }
            user.setTeacherStudentNo(teacher.getNo());
            boolean result = userService.updateById(user);
            if (result){
                return new Result(200,"绑定教师成功！");
            }
        }
        return new Result(201,"绑定教师失败！");
    }

    // 根据教工号获取监考信息列表
    @Override
    public Result getTeacherExams(Long teacherNo) {
        if (teacherNo != null){
            List<Exam> exams = examService.getExamsByInvigilator(teacherNo);
            List<InvigilatorExam> invigilatorExams = new ArrayList<>();
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            exams.forEach(exam -> {
                InvigilatorExam invigilatorExam = new InvigilatorExam();
                Date start = new Date(exam.getExamStart().getTime() - 8*60*60*1000);
                Date end = new Date(exam.getExamEnd().getTime() - 8*60*60*1000);
                String startStr = sf.format(start);
                String endStr = sf.format(end).substring(11);
                invigilatorExam.setInvigilateTime(startStr + "-" + endStr);
                invigilatorExam.setInvigilateRoom(exam.getClassRoom());
                invigilatorExam.setInvigilateDuration((int) ((end.getTime() - start.getTime()) / 1000 /60));
                if (exam.getExamEnd().before(new Date())){
                    invigilatorExam.setInvigilateStatus("已结束");
                }else {
                    invigilatorExam.setInvigilateStatus("进行中");
                }
                invigilatorExam.setInvigilateVenue(exam.getTeachBuilding());
                invigilatorExam.setInvigilateSubject(exam.getExamSubject());
                invigilatorExams.add(invigilatorExam);
            });
            return new Result(200,"获取监考信息列表成功！",invigilatorExams);
        }
        return new Result(201,"获取监考信息列表失败！");
    }

    // 接收excel文件，批量插入教师信息
    @Override
    public Result saveTeachers(List<List<Object>> lists) {
        List<Teacher> teachers = new ArrayList<>();
        lists.forEach(list -> {
            Teacher teacher = new Teacher();
            teacher.setNo(Integer.valueOf(list.get(1).toString()));
            teacher.setName(list.get(2).toString());
            teacher.setSex(list.get(3).toString());
            teacher.setCollege(list.get(4).toString());
            teacher.setPosition(list.get(5).toString());
            teacher.setPhone(list.get(6).toString());
            Teacher one = getOne(new QueryWrapper<Teacher>().eq("no", teacher.getNo()));
            if (one == null){
                teachers.add(teacher);
            }
        });
        if (teachers.isEmpty()){
            return new Result(201,"导入失败，文件内容已存在！");
        }
        boolean result = saveBatch(teachers, teachers.size());
        if (result){
            redisCache.deleteObject("teacher");
            return new Result(200,"导入成功！");
        }
        return new Result(201,"导入失败！");
    }

    // 删除所有的监考老师
    @Override
    public Result deleteAll() {
        int delete = baseMapper.delete(null);
        if (delete > 0){
            redisCache.deleteObject("teacher");
            return new Result(200,"已删除！");
        }
        return new Result(201,"删除失败！");
    }
}
