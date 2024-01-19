package com.jgs.collegeexamsystemback.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgs.collegeexamsystemback.dto.CollegeStudents;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.CollegeMapper;
import com.jgs.collegeexamsystemback.pojo.*;
import com.jgs.collegeexamsystemback.service.StudentService;
import com.jgs.collegeexamsystemback.service.UserService;
import com.jgs.collegeexamsystemback.vo.StudentQueryVo;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author Administrator
 * @version 1.0
 * @description 考生控制类
 * @date 2023/7/21 0021 11:52
 */
@RestController
@RequestMapping("student")
public class StudentController {

    @Resource
    private StudentService studentService;
    @Resource
    private CollegeMapper collegeMapper;
    @Resource
    private UserService userService;
    @Resource
    private RedisCache redisCache;


    /**
     * @description 根据条件分页查询学生列表
     * @author Administrator
     * @date 11:55
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("getStudentList/{pageNo}/{pageSize}")
    public Result getStudentPageList(@PathVariable int pageNo, @PathVariable int pageSize, @RequestBody StudentQueryVo studentQueryVo) {
        String redisKey = studentQueryVo.getName() + studentQueryVo.getClassName() + studentQueryVo.getCollege() + studentQueryVo.getProfession() + studentQueryVo.getGrade() + pageNo + "_" + pageSize;
        Page<Student> studentPage = redisCache.getCacheMapValue("student",redisKey);
        if (studentPage == null){
            Page<Student> page = new Page<>(pageNo, pageSize);
            Page<Student> studentPageList = studentService.getStudentPageList(page, studentQueryVo);
            redisCache.setCacheMapValue("student",redisKey,studentPageList);
            return Result.ok(studentPageList);
        }
        return Result.ok(studentPage);
    }

    /**
     * @description 添加学生
     * @author Administrator
     * @date 15:27
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("addStudent")
    public Result saveStudent(@RequestBody Student student) {
        return studentService.saveStudent(student);
    }

    /**
     * 接收excel文件，批量导入学生信息
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("receiveExcel")
    public Result receiveExcel(@RequestParam("file")MultipartFile file){
        try{
            // 使用WorkbookFactory校验文件是否为excel文件
            Workbook sheets = WorkbookFactory.create(file.getInputStream());
            if (sheets != null){
                // 解析excel文件
                List<List<Object>> lists = ExcelUtil.getReader(file.getInputStream()).read(2);
                sheets.close();
                // 将数据存入数据库
                return studentService.saveStudents(lists);
            }
            return Result.fail("接收失败！");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("文件格式错误！");
        }
    }

    /**
     * @description 根据id删除学生
     * @author Administrator
     * @date 15:53
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteStudent/{id}")
    public Result deleteStudent(@PathVariable Long id) {
        return studentService.deleteStudent(id);
    }

    /**
     * @description 批量删除学生
     * @author Administrator
     * @date 15:56
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteStudents")
    public Result deleteStudents(@RequestBody List<Student> students) {
        return studentService.deleteStudents(students);
    }

    /**
     * 删除所有学生
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteAll")
    public Result deleteAll(){
        return studentService.deleteAll();
    }

    /**
     * @description 更新学生信息
     * @author Administrator
     * @date 16:23
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("updateStudent")
    public Result updateStudent(@RequestBody Student student) {
        return studentService.updateStudent(student);
    }

    /**
     * @description 根据学号获取学生信息
     * @author Administrator
     * @date 21:01
     */
    @PreAuthorize("hasAuthority('学生')")
    @PostMapping("getStudent/{studentNo}")
    public Result getStudent(@PathVariable Long studentNo) {
        if (studentNo != null) {
            return Result.ok(studentService.getOne(new QueryWrapper<Student>().eq("no", studentNo)));
        }
        return Result.fail();
    }

    /**
     * @description 绑定学生信息
     * @author Administrator
     * @date 21:43
     */
    @PreAuthorize("hasAuthority('学生')")
    @GetMapping("bindStudent/{userId}/{studentNo}")
    public Result bindStudent(@PathVariable Integer userId, @PathVariable Long studentNo) {
        if (userId != null && studentNo != null){
            User user = userService.getById(userId);
            Student student = studentService.getOne(new QueryWrapper<Student>().eq("no", studentNo));
            if (student == null){
                return new Result(201,"绑定失败，不存在该学生！");
            }
            // 判断该学生是否已被绑定
            User isExist = userService.getOne(new QueryWrapper<User>().eq("teacher_student_no", student.getNo()));
            if (!ObjectUtil.isNull(isExist)) {
                return new Result(201, "绑定失败，该学生已被绑定！");
            }
            user.setTeacherStudentNo(student.getNo());
            boolean result = userService.updateById(user);
            if (result) {
                return new Result(200, "绑定学生成功！");
            }
        }

        return new Result(201, "绑定学生失败！");
    }

    /**
    * @description 解绑学生
    * @author Administrator
    * @date  22:41
    */
    @PreAuthorize("hasAuthority('学生')")
    @GetMapping("unbindStudent/{userId}")
    public Result unbindStudent(@PathVariable Integer userId){
        if (userId != null){
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("teacher_student_no",null).eq("user_id",userId);
            boolean update = userService.update(updateWrapper);
            if (update){
                return Result.ok();
            }
        }
        return Result.fail();
    }

    /**
     * @description 根据学号查询学生考试信息列表
     * @author Administrator
     * @date 13:52
     */
    @PreAuthorize("hasAuthority('学生')")
    @GetMapping("getStudentExams/{studentNo}")
    public Result getStudentExams(@PathVariable Long studentNo) {
        return studentService.getStudentExams(studentNo);
    }

    /**
    * @description 获取学院学生数
    * @author Administrator
    * @date  21:50
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getCollegeStudents")
    public Result getStudentsByCollege(){
        List<College> collegeProfessions = collegeMapper.selectList(null);
        Set<String> colleges = new HashSet<>();
        collegeProfessions.forEach(college -> {
            colleges.add(college.getCollege());
        });
        List<CollegeStudents> collegeStudents = new ArrayList<>();
        colleges.forEach(collegeName -> {
            List<Student> students = studentService.getBaseMapper().selectList(new QueryWrapper<Student>().eq("college", collegeName));
            CollegeStudents cs = new CollegeStudents();
            cs.setCollege(collegeName);
            cs.setStudents(students.size());
            collegeStudents.add(cs);
        });
        return Result.ok(collegeStudents);
    }

    /**
    * @description 获取学生总数
    * @author Administrator
    * @date  14:44
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getStudents")
    public Result getStudents(){
        List<Student> students = studentService.getBaseMapper().selectList(null);
        return Result.ok(students.size());
    }

    /**
    * @description 添加学生公共班级
    * @author Administrator
    * @date  13:28
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("addElectiveClass/{studentId}")
    public Result addElectiveClass(@RequestBody Map<String,String> map,@PathVariable Long studentId){
        return studentService.addElectiveClass(map,studentId);
    }

    /**
    * @description 移除学生公共班级
    * @author Administrator
    * @date  14:32
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("removeElectiveClass")
    public Result removeElectiveClass(@RequestParam Long studentId,@RequestParam String className){
        return studentService.removeElectiveClass(studentId,className);
    }
}
