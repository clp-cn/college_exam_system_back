package com.jgs.collegeexamsystemback.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.dto.StudentExam;
import com.jgs.collegeexamsystemback.mapper.StudentClassMapper;
import com.jgs.collegeexamsystemback.mapper.StudentMapper;
import com.jgs.collegeexamsystemback.pojo.*;
import com.jgs.collegeexamsystemback.service.*;
import com.jgs.collegeexamsystemback.vo.StudentQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Administrator
 * @version 1.0
 * @description StudentServiceImpl
 * @date 2023/7/18 0018 22:52
 */
@Service
@Transactional
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    @Resource
    private ExamService examService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ProfessionClassService professionClassService;
    @Resource
    private ExamClassService examClassService;
    @Resource
    private ExamRoomService examRoomService;
    @Resource
    private StudentClassMapper studentClassMapper;
    @Resource
    private StudentClassService studentClassService;

    // 根据条件分页查询学生列表
    @Override
    public Page<Student> getStudentPageList(Page<Student> page, StudentQueryVo studentQueryVo) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name",studentQueryVo.getName())
                .like("college",studentQueryVo.getCollege())
                .like("profession",studentQueryVo.getProfession())
                .like("grade",studentQueryVo.getGrade())
                .like("class_name",studentQueryVo.getClassName())
                .orderByAsc("no");
        Page<Student> studentPage = baseMapper.selectPage(page, queryWrapper);
        List<Student> records = studentPage.getRecords();
        records.forEach(r -> {
            List<StudentClass> studentClasses = studentClassMapper.selectList(new QueryWrapper<StudentClass>().eq("student_id", r.getId()));
            List<Long> classIds = new ArrayList<>();
            studentClasses.forEach(studentClass -> classIds.add(studentClass.getExamClassId()));
            List<ExamClass> classes = examClassService.getBaseMapper().selectBatchIds(classIds);
            List<String> classNames = new ArrayList<>();
            classes.forEach(c -> classNames.add(c.getName()));
            classNames.remove(r.getClassName());
            if (classNames.isEmpty()){
                classNames.add("暂无");
                r.setElectiveClass(classNames);
            }else {
                r.setElectiveClass(classNames);
            }
        });
        return studentPage.setRecords(records);
    }

    // 根据学号查询学生考试信息列表
    @Override
    public Result getStudentExams(Long studentNo) {
        if (studentNo != null) {
            Student student = baseMapper.selectOne(new QueryWrapper<Student>().eq("no", studentNo));
            if (ObjectUtil.isNull(student)){
                return new Result(201,"该学生不存在！");
            }
            int seat = Integer.parseInt(String.valueOf(student.getNo()).substring(student.getNo().toString().length() - 2));
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<StudentExam> studentExams = new ArrayList<>();
            // 查询学生关联班级
            List<StudentClass> studentClasses = studentClassMapper.selectList(new QueryWrapper<StudentClass>().eq("student_id", student.getId()));
            // 查询所有班级
            List<Long> classIds = new ArrayList<>();
            studentClasses.forEach(studentClass -> classIds.add(studentClass.getExamClassId()));
            List<ExamClass> classes = examClassService.getClassesByIds(classIds);
            // 根据班级查询所有的考试安排
            classes.forEach(className -> {
                List<Exam> examList = examService.getExamsByClassName(className.getName());
                examList.forEach(exam -> {
                    List<Exam> exams = examService.getBaseMapper().selectList(new QueryWrapper<Exam>().eq("exam_subject", exam.getExamSubject()).eq("exam_class", exam.getExamClass()));
                    if(exams.size() > 1){
                        Set<String> ses = new HashSet<>();
                        studentExams.forEach(se -> {
                            ses.add(se.getExamSubject());
                        });
                        if (!ses.contains(exam.getExamSubject())){
                            StudentExam studentExam = new StudentExam();
                            List<ExamRoom> examRooms = new ArrayList<>();
                            exams.forEach(e -> {
                                ExamRoom examRoom = examRoomService.getOne(new QueryWrapper<ExamRoom>().eq("teach_building", e.getTeachBuilding()).eq("name", e.getClassRoom()));
                                examRooms.add(examRoom);
                            });
                            if (seat > examRooms.get(0).getCapacity()/2){
                                studentExam.setSeatNumber(seat - examRooms.get(0).getCapacity()/2);
                                studentExam.setExamVenue(examRooms.get(1).getTeachBuilding());
                                studentExam.setExamRoom(examRooms.get(1).getName());
                            }else {
                                studentExam.setSeatNumber(seat);
                                studentExam.setExamVenue(examRooms.get(0).getTeachBuilding());
                                studentExam.setExamRoom(examRooms.get(0).getName());
                            }
                            if (exam.getExamEnd().before(new Date())){
                                studentExam.setExamStatus("已结束");
                            }else {
                                studentExam.setExamStatus("进行中");
                            }
                            studentExam.setExamSubject(exam.getExamSubject());
                            Date start = new Date(exam.getExamStart().getTime() - 8*60*60*1000);
                            Date end = new Date(exam.getExamEnd().getTime() - 8*60*60*1000);
                            String startStr = sf.format(start);
                            String endStr = sf.format(end).substring(11);
                            studentExam.setExamDuration((int) ((end.getTime() - start.getTime()) / 1000 /60));
                            studentExam.setExamTime(startStr + "-" + endStr);
                            studentExams.add(studentExam);
                        }
                    }else {
                        StudentExam studentExam = new StudentExam();
                        if (exam.getExamEnd().before(new Date())){
                            studentExam.setExamStatus("已结束");
                        }else {
                            studentExam.setExamStatus("进行中");
                        }
                        studentExam.setSeatNumber(seat);
                        studentExam.setExamRoom(exam.getClassRoom());
                        studentExam.setExamSubject(exam.getExamSubject());
                        studentExam.setExamVenue(exam.getTeachBuilding());
                        Date start = new Date(exam.getExamStart().getTime() - 8*60*60*1000);
                        Date end = new Date(exam.getExamEnd().getTime() - 8*60*60*1000);
                        String startStr = sf.format(start);
                        String endStr = sf.format(end).substring(11);
                        studentExam.setExamDuration((int) ((end.getTime() - start.getTime()) / 1000 /60));
                        studentExam.setExamTime(startStr + "-" + endStr);
                        studentExams.add(studentExam);
                    }
                });
            });
            studentExams.sort(Comparator.comparing(StudentExam::getExamTime));
            return new Result(200,"获取考试信息成功！",studentExams);
        }
        return new Result(201,"获取考试信息失败！");
    }

    // 添加学生
    @Override
    public Result saveStudent(Student student) {
        if (student != null){
            // 根据学号查询学生是否已存在
            Student select = baseMapper.selectOne(new QueryWrapper<Student>().eq("no", student.getNo()));
            if (!ObjectUtil.isNull(select)) {
                return new Result(201, "学号重复，请输入正确可用的学号！");
            }
            // 查询班级是否存在
            ProfessionClass professionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", student.getClassName()));
            if (professionClass == null){
                return new Result(201,"添加失败，班级不存在，请输入正确的班级！");
            }
            int result = baseMapper.insert(student);
            if (result > 0) {
                Student selectOne = baseMapper.selectOne(new QueryWrapper<Student>().eq("no", student.getNo()));
                // 向student_class_id表中插入数据
                StudentClass studentClass = new StudentClass();
                studentClass.setStudentId(selectOne.getId());
                studentClass.setProfessionClassId(professionClass.getId());
                studentClass.setExamClassId(professionClass.getId());
                int insert = studentClassMapper.insert(studentClass);
                if (insert > 0){
                    redisCache.deleteObject("student");
                    return new Result(200,"添加成功！");
                }
            }
        }

        return new Result(201,"添加失败！");
    }

    // 批量添加学生
    @Override
    public Result saveStudents(List<List<Object>> lists) {
        List<Student> students = new ArrayList<>();
        for(List<Object> list:lists){
            Student student = new Student();
            student.setNo(Integer.valueOf(list.get(1).toString()));
            student.setName(list.get(2).toString());
            student.setSex(list.get(3).toString());
            student.setCollege(list.get(4).toString());
            student.setGrade(list.get(5).toString());
            student.setProfession(list.get(6).toString());
            student.setClassName(list.get(7).toString());
            List<String> electiveClasses = null;
            if (!(Objects.equals(list.get(8).toString(), ""))){
                electiveClasses = Arrays.asList(list.get(8).toString().split(","));
            }
            student.setElectiveClass(electiveClasses);
            student.setPhone(list.get(9).toString());
            // 根据学号查询学生是否已存在
            Student select = baseMapper.selectOne(new QueryWrapper<Student>().eq("no", student.getNo()));
            // 查询班级是否存在
            ProfessionClass professionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", student.getClassName()));
            if (select == null && professionClass != null){
                students.add(student);
            }
        }
        if (students.isEmpty()){
            return new Result(201,"导入失败，文件内容已存在！");
        }
        boolean result = saveBatch(students, students.size());
        if(result){
            List<StudentClass> studentClasses = new ArrayList<>();
            for (Student s:students){
                Student student = baseMapper.selectOne(new QueryWrapper<Student>().eq("no", s.getNo()));
                ExamClass examClass = examClassService.getOne(new QueryWrapper<ExamClass>().eq("name", s.getClassName()));
                ProfessionClass professionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", s.getClassName()));
                List<String> electiveClasses = s.getElectiveClass();
                StudentClass studentClass = new StudentClass();
                studentClass.setStudentId(student.getId());
                studentClass.setProfessionClassId(professionClass.getId());
                studentClass.setExamClassId(examClass.getId());
                studentClasses.add(studentClass);
                if (electiveClasses != null && !electiveClasses.isEmpty()){
                    for(String electiveClass:electiveClasses){
                        StudentClass studentClass1 = new StudentClass();
                        ExamClass examClass1 = examClassService.getOne(new QueryWrapper<ExamClass>().eq("name", electiveClass));
                        if (examClass1 == null){
                            return new Result(201,"导入失败！");
                        }
                        studentClass1.setStudentId(student.getId());
                        studentClass1.setProfessionClassId(professionClass.getId());
                        studentClass1.setExamClassId(examClass1.getId());
                        studentClasses.add(studentClass1);
                    }
                }
            }
            if (studentClasses.isEmpty()){
                return new Result(201,"导入失败，文件内容已存在！");
            }
            boolean saveResult = studentClassService.saveBatch(studentClasses, studentClasses.size());
            if(saveResult){
                redisCache.deleteObject("student");
                return new Result(200,"导入成功！");
            }
        }
        return new Result(201,"导入失败！");
    }

    // 删除所有学生
    @Override
    public Result deleteAll() {
        List<Student> students = baseMapper.selectList(null);
        List<Long> studentIds = new ArrayList<>();
        students.forEach(s -> studentIds.add(s.getId()));
        if (studentIds.isEmpty()){
            return new Result(201,"删除失败！");
        }
        boolean remove = studentClassService.remove(new QueryWrapper<StudentClass>().in("student_id", studentIds));
        int delete = baseMapper.delete(null);
        if (delete > 0 && remove){
            redisCache.deleteObject("student");
            return new Result(200,"删除成功！");
        }
        return new Result(201,"删除失败！");
    }

    // 根据id删除学生
    @Override
    public Result deleteStudent(Long id) {
        if (id != null){
            int result = baseMapper.deleteById(id);
            if (result > 0) {
                // 向student_class_id表中删除关联数据
                List<StudentClass> studentClasses = studentClassMapper.selectList(new QueryWrapper<StudentClass>().eq("student_id", id));
                ArrayList<Long> ids = new ArrayList<>();
                studentClasses.forEach(studentClass -> ids.add(studentClass.getId()));
                int delete = studentClassMapper.deleteBatchIds(ids);
                if (delete > 0){
                    redisCache.deleteObject("student");
                    return new Result(200, "删除成功！");
                }
            }
        }
        return new Result(201, "操作失败，请检查网络或重试！");
    }

    // 批量删除学生
    @Override
    public Result deleteStudents(List<Student> students) {
        if (students != null && !students.isEmpty()){
            List<Long> ids = new ArrayList<>();
            students.forEach(student -> ids.add(student.getId()));
            int result = baseMapper.deleteBatchIds(ids);
            if (result > 0) {
                int flag = 0;
                for (Long id:ids) {
                    List<StudentClass> studentClasses = studentClassMapper.selectList(new QueryWrapper<StudentClass>().eq("student_id", id));
                    ArrayList<Long> scIds = new ArrayList<>();
                    studentClasses.forEach(studentClass -> scIds.add(studentClass.getId()));
                    flag = studentClassMapper.deleteBatchIds(scIds);
                }
                if (flag > 0){
                    redisCache.deleteObject("student");
                    return new Result(200, "删除成功！");
                }
            }
        }
        return new Result(201, "操作失败，请检查网络或重试！");
    }

    // 更新学生信息
    @Override
    public Result updateStudent(Student student) {
        if (student != null){
            Student selectStudent = baseMapper.selectById(student.getId());

            ProfessionClass selectProfessionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", selectStudent.getClassName()));
            int result = baseMapper.updateById(student);
            if (result > 0) {
                ProfessionClass professionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", student.getClassName()));
                if (professionClass == null){
                    return new Result(201,"更新失败，班级不存在！");
                }
                StudentClass studentClass = studentClassMapper.selectOne(new QueryWrapper<StudentClass>().
                        eq("student_id", student.getId()).
                        eq("profession_class_id",selectProfessionClass.getId()).
                        eq("exam_class_id",selectProfessionClass.getId()));
                studentClass.setExamClassId(professionClass.getId());
                int update = studentClassMapper.updateById(studentClass);
                if (update > 0){
                    redisCache.deleteObject("student");
                    return new Result(200, "更新成功！");
                }
            }
        }
        return new Result(201, "操作失败，请重试！");
    }

    // 添加学生公共班级
    @Override
    public Result addElectiveClass(Map<String, String> map, Long studentId) {
        if (studentId != null){
            // 查询该班级是否存在
            QueryWrapper<ExamClass> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("college",map.get("college"))
                    .eq("grade",map.get("grade"))
                    .eq("name",map.get("className"));
            ExamClass examClass = examClassService.getOne(queryWrapper);
            if (examClass == null){
                return new Result(201,"添加失败，该公共班级不存在！");
            }
            // 向student_class表中插入数据
            Student student = baseMapper.selectById(studentId);
            ProfessionClass professionClass = professionClassService.getOne(new QueryWrapper<ProfessionClass>().eq("name", student.getClassName()));
            StudentClass studentClass = new StudentClass();
            studentClass.setStudentId(studentId);
            studentClass.setProfessionClassId(professionClass.getId());
            studentClass.setExamClassId(examClass.getId());
            int insert = studentClassMapper.insert(studentClass);
            if (insert > 0){
                redisCache.deleteObject("student");
                return new Result(200,"添加成功！");
            }
        }
        return new Result(201,"添加失败！");
    }

    // 移除学生公共班级
    @Override
    public Result removeElectiveClass(Long studentId,String className) {
        if (studentId != null && className != null){
            List<StudentClass> studentClasses = studentClassMapper.selectList(new QueryWrapper<StudentClass>().eq("student_id", studentId));
            List<Long> ids = new ArrayList<>();
            studentClasses.forEach(studentClass -> ids.add(studentClass.getExamClassId()));
            ExamClass examClass = examClassService.getOne(new QueryWrapper<ExamClass>().eq("name", className));
            ids.remove(examClass.getId());
            List<Long> studentClassIds = new ArrayList<>();
            ids.forEach(id -> {
                StudentClass studentClass = studentClassMapper.selectOne(new QueryWrapper<StudentClass>().eq("student_id",studentId).eq("exam_class_id", id));
                studentClassIds.add(studentClass.getId());
            });
            int result = studentClassMapper.deleteBatchIds(studentClassIds);
            if (result > 0){
                redisCache.deleteObject("student");
                return new Result(200,"移除成功！");
            }
        }
        return new Result(201,"移除失败！");
    }

}
