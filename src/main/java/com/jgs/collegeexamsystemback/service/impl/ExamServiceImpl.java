package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.ExamMapper;
import com.jgs.collegeexamsystemback.pojo.*;
import com.jgs.collegeexamsystemback.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamServiceImpl
 * @date 2023/7/19 0019 13:17
 */
@Service
@Transactional
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {
    @Resource
    private TeacherService teacherService;
    @Resource
    private ExamRoomService examRoomService;
    @Resource
    private ExamSubjectService examSubjectService;
    @Resource
    private ExamDateService examDateService;
    @Resource
    private ExamCardService examCardService;
    @Resource
    private ProfessionClassService professionClassService;
    @Resource
    private ExamClassService examClassService;
    @Resource
    private SubjectClassService subjectClassService;
    @Resource
    private RedisCache redisCache;


    // 获取排考分页列表
    @Override
    public Result getExamList(Integer pageNo,Integer pageSize){
        String redisKey = pageNo + "_" + pageSize;
        Page<Exam> examList = redisCache.getCacheMapValue("exam", redisKey);
        if (examList == null){
            Page<Exam> examPage = new Page<>(pageNo,pageSize);
            Page<Exam> examList1 = baseMapper.selectPage(examPage, null);
            List<Exam> records = examList1.getRecords();
            records.sort(Comparator.comparing(Exam::getExamStart));
            records.forEach(r -> {
                Teacher teacher = teacherService.getOne(new QueryWrapper<Teacher>().eq("no", r.getInvigilator()));
                r.setInvigilatorName(teacher.getName());
                Date start = new Date(r.getExamStart().getTime() - 8 * 60 * 60 * 1000);
                Date end = new Date(r.getExamEnd().getTime() - 8 * 60 * 60 * 1000);
                r.setExamStart(start);
                r.setExamEnd(end);
            });
            examList1.setRecords(records);
            redisCache.setCacheMapValue("exam",redisKey,examList1);
            return Result.ok(examList1);
        }
        return Result.ok(examList);
    }

    // 根据班级名称获取考试列表
    @Override
    public List<Exam> getExamsByClassName(String className) {
        return baseMapper.selectList(new QueryWrapper<Exam>().eq("exam_class",className).orderByAsc("exam_start"));
    }

    // 根据教工号获取考试列表
    @Override
    public List<Exam> getExamsByInvigilator(Long teacherNo) {
        return baseMapper.selectList(new QueryWrapper<Exam>().eq("invigilator",teacherNo).orderByAsc("exam_start"));
    }

    // 删除所有的考试安排
    @Override
    public Result deleteAll() {
        int delete = baseMapper.delete(null);
        if (delete > 0){
            redisCache.deleteObject("exam");
            examCardService.deleteExamCardAll();
        }
        return new Result(201,"删除失败！");
    }

    // 查询考试天数和班级科目数的冲突关系（一天只允许一个班级考一门）
    @Override
    public boolean isClash(int i) {
        // 查询所有班级
        List<ProfessionClass> professionClasses = professionClassService.getAll();
        List<Integer> subjects = new ArrayList<>();
        professionClasses.forEach(studentClass -> {
            // 根据班级id查询所有的科目
            List<SubjectClass> subjectClasses = subjectClassService.getBaseMapper().selectList(new QueryWrapper<SubjectClass>().eq("class_id", studentClass.getId()));
            subjects.add(subjectClasses.size());
        });
        // 获取subjects中的最大值并与天数比较
        Integer max = Collections.max(subjects);
        return max > i;
    }

    // 接收excel文件，批量插入考试安排数据
    @Override
    public Result saveExams(List<List<Object>> lists) {
        List<Exam> exams = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        lists.forEach(list -> {
            try {
                Exam exam = new Exam();
                exam.setExamSubject(list.get(1).toString());
                exam.setTeachBuilding(list.get(2).toString());
                exam.setClassRoom(list.get(3).toString());
                exam.setInvigilator(Integer.valueOf(list.get(4).toString()));
                exam.setExamClass(list.get(5).toString());
                exam.setExamStart(dateFormat.parse(list.get(6).toString()));
                exam.setExamEnd(dateFormat.parse(list.get(7).toString()));
                Exam one = getOne(new QueryWrapper<Exam>().eq("exam_start", exam.getExamStart()).
                        eq("exam_class", exam.getExamClass()).
                        eq("exam_subject", exam.getExamSubject()).
                        eq("teach_building", exam.getTeachBuilding()).
                        eq("class_room", exam.getClassRoom()).
                        eq("invigilator", exam.getInvigilator()));
                if (one == null){
                    exams.add(exam);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        if (exams.isEmpty()){
            return new Result(201,"导入失败，文件内容已存在！");
        }
        boolean result = saveBatch(exams, exams.size());
        if (result){
            redisCache.deleteObject("exam");
            return new Result(200,"导入成功！");
        }
        return new Result(201,"导入失败，请重试！");
    }

    // 自动排考
    @Override
    public void schedulerExams(){
        // 获取所有的教师
        List<Teacher> teachers = teacherService.getAll();
        // 获取所有的教室
        List<ExamRoom> examRooms = examRoomService.getAll();
        // 获取所有的考试科目
        List<ExamSubject> subjects = examSubjectService.getAll();
        // 获取所有的考试时间
        List<ExamDate> dates = examDateService.getAll();
        for (ExamSubject examSubject:subjects) {
            // 查询该科目待考的所有的班级
            List<SubjectClass> subjectClasses = subjectClassService.getBaseMapper().selectList(new QueryWrapper<SubjectClass>().eq("subject_id", examSubject.getId()));
            List<Long> classIds = new ArrayList<>();
            subjectClasses.forEach(subjectClass -> classIds.add(subjectClass.getClassId()));
            List<ExamClass> classes = examClassService.getClassesByIds(classIds);
            for (ExamClass examClass : classes){
                // 查询可用的时间
                ExamDate availableExamDate = findAvailableExamDate(dates,examRooms,teachers,examClass);
                if (availableExamDate == null){
                    break;
                }
                // 查询可用的教室
                List<ExamRoom> availableExamRooms = findAvailableExamRoom(availableExamDate,examClass,examRooms);
                // 查询可用的教师
                List<Teacher> availableTeachers = findAvailableExamTeacher(availableExamDate,teachers,availableExamRooms);
                // 如果有可用的时间、教室、教师，则进行安排考试
                if (!availableExamRooms.isEmpty() && !availableTeachers.isEmpty()) {
                    for (int i = 0; i < availableExamRooms.size(); i++) {
                        Exam exam = new Exam();
                        exam.setExamStart(availableExamDate.getTime());
                        exam.setExamEnd(new Date(availableExamDate.getTime().getTime() + examSubject.getDuration() * 60 * 1000));
                        exam.setExamSubject(examSubject.getName());
                        exam.setExamClass(examClass.getName());
                        exam.setTeachBuilding(availableExamRooms.get(i).getTeachBuilding());
                        exam.setClassRoom(availableExamRooms.get(i).getName());
                        exam.setInvigilator(availableTeachers.get(i).getNo());
                        baseMapper.insert(exam);
                    }
                }else {
                    System.out.println("无法安排考试：" + examSubject + "-------" + examClass);
                }
            }
        }
    }

    // 查询可用的时间
    private ExamDate findAvailableExamDate(List<ExamDate> dates, List<ExamRoom> examRooms, List<Teacher> teachers, ExamClass examClass){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (ExamDate examDate: dates){
            // 查询该时间所以的考试安排
            List<Exam> exams = baseMapper.selectList(new QueryWrapper<Exam>().eq("exam_start", examDate.getTime()));
            // 查询该日期该班级的所有考试安排
            List<Exam> studentClassExams = baseMapper.selectList(new QueryWrapper<Exam>().like("exam_start", simpleDateFormat.format(examDate.getTime())).eq("exam_class", examClass.getName()));
            if (exams.size() < examRooms.size() && exams.size() < teachers.size() && studentClassExams.isEmpty()){
                return examDate;
            }
        }
        return null;
    }

    // 查询可用的教室
    private List<ExamRoom> findAvailableExamRoom(ExamDate examDate, ExamClass examClass, List<ExamRoom> examRooms){
        // 查询该时间的所有考试安排
        List<Exam> exams = baseMapper.selectList(new QueryWrapper<Exam>().eq("exam_start", examDate.getTime()));
        // 查询该时间占用的所有教室
        List<ExamRoom> occupyExamRooms = new ArrayList<>();
        exams.forEach(exam -> {
            ExamRoom one = examRoomService.getOne(new QueryWrapper<ExamRoom>().eq("teach_building", exam.getTeachBuilding()).eq("name", exam.getClassRoom()));
            occupyExamRooms.add(one);
        });
        List<ExamRoom> examRoomSet = new ArrayList<>();
        int number = examClass.getStudents();
        for (ExamRoom examRoom: examRooms){
            if (!occupyExamRooms.contains(examRoom) && examRoom.getCapacity() >= number*2){
                examRoomSet.add(examRoom);
                return examRoomSet;
            }else if (!occupyExamRooms.contains(examRoom) && examRoom.getCapacity() < number*2){
                examRoomSet.add(examRoom);
                number = number/2;
            }
        }
        return examRoomSet;
    }

    // 查询可用的教师（教师一天可监考两场）
    private List<Teacher> findAvailableExamTeacher(ExamDate examDate,List<Teacher> teachers,List<ExamRoom> examRooms){
        // 查询该日期的所有考试安排
        List<Exam> exams = baseMapper.selectList(new QueryWrapper<Exam>().eq("exam_start", examDate.getTime()));
        // 查询该日期占用的所有教师
        List<Teacher> occupyTeachers = new ArrayList<>();
        exams.forEach(exam -> {
            Teacher one = teacherService.getOne(new QueryWrapper<Teacher>().eq("no", exam.getInvigilator()));
            occupyTeachers.add(one);
        });
        List<Teacher> teacherSet = new ArrayList<>();
        for (int i = 0;i<examRooms.size();i++){
            for (Teacher teacher: teachers){
                if (!occupyTeachers.contains(teacher)){
                    teacherSet.add(teacher);
                }
            }
        }
        return teacherSet;
    }

}
