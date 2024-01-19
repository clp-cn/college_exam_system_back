package com.jgs.collegeexamsystemback.controller;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.Teacher;
import com.jgs.collegeexamsystemback.pojo.User;
import com.jgs.collegeexamsystemback.service.TeacherService;
import com.jgs.collegeexamsystemback.service.UserService;
import com.jgs.collegeexamsystemback.vo.InvigilatorVo;
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
 * @description 教师控制类
 * @date 2023/7/27 0027 11:41
 */
@RestController
@RequestMapping("teacher")
public class TeacherController {
    @Resource
    private TeacherService teacherService;
    @Resource
    private UserService userService;
    @Resource
    private RedisCache redisCache;

    /**
     * @description 根据条件分页查询监考老师列表
     * @author Administrator
     * @date  16:23
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("getInvigilatorList/{pageNo}/{pageSize}")
    public Result getInvigilatorList(@PathVariable Integer pageNo, @PathVariable Integer pageSize, @RequestBody InvigilatorVo invigilatorVo){
        String redisKey = invigilatorVo.getName() + "_" +
                invigilatorVo.getCollege() + "_" +
                invigilatorVo.getPosition() + "_" +
                pageNo + "_" + pageSize;
        Page<Teacher> teacherPage = redisCache.getCacheMapValue("teacher",redisKey);
        if (teacherPage == null){
            Page<Teacher> page = new Page<>(pageNo, pageSize);
            Page<Teacher> invigilatorPageList = teacherService.getInvigilatorPageList(page,invigilatorVo);
            redisCache.setCacheMapValue("teacher",redisKey,invigilatorPageList);
            return Result.ok(invigilatorPageList);
        }
        return Result.ok(teacherPage);
    }

    /**
     * @description 新增监考老师
     * @author Administrator
     * @date  17:03
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("saveInvigilator")
    public Result saveInvigilator(@RequestBody Teacher teacher){
        if (teacher != null){
            Teacher select = teacherService.getOne(new QueryWrapper<Teacher>().eq("no", teacher.getNo()));
            if (select != null){
                return new Result(201,"添加失败，请勿重复添加！");
            }
            boolean result = teacherService.save(teacher);
            if (result){
                redisCache.deleteObject("teacher");
                return new Result(200,"添加监考老师成功！");
            }
        }
        return new Result(201,"操作失败，请检查网络或重试！");
    }

    /**
     * 接收excel文件，批量插入数据
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
                return teacherService.saveTeachers(lists);
            }
            return new Result(201,"接收失败！");
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(201,"文件格式错误！");
        }
    }

    /**
     * @description 删除监考老师
     * @author Administrator
     * @date  17:09
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteInvigilator/{id}")
    public Result deleteInvigilator(@PathVariable Long id){
        if (id != null){
            boolean result = teacherService.removeById(id);
            if (result){
                redisCache.deleteObject("teacher");
                return new Result(200,"删除成功！");
            }
        }
        return new Result(201,"删除失败，请重试！");
    }

    /**
     * @description 批量删除监考老师
     * @author Administrator
     * @date  17:11
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteInvigilators")
    public Result deleteInvigilators(@RequestBody List<Teacher> teachers){
        if (teachers != null && !teachers.isEmpty()){
            List<Long> ids = new ArrayList<>();
            teachers.forEach(teacher -> ids.add(teacher.getId()));
            boolean result = teacherService.removeBatchByIds(ids);
            if (result){
                redisCache.deleteObject("teacher");
                return new Result(200,"删除成功！");
            }
        }
        return new Result(201,"删除失败，请重试！");
    }

    /**
     * 删除所有的监考老师
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteAll")
    public Result deleteAll(){
        return teacherService.deleteAll();
    }

    /**
     * @description 更新监考老师信息
     * @author Administrator
     * @date  17:13
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("updateInvigilator")
    public Result updateInvigilator(@RequestBody Teacher teacher){
        if (teacher != null){
            boolean result = teacherService.updateById(teacher);
            if (result){
                redisCache.deleteObject("teacher");
                return new Result(200,"更新成功！");
            }
        }
        return new Result(201,"操作失败！");
    }

    /**
    * @description 根据教工号获取教师信息
    * @author Administrator
    * @date  11:48
    */
    @PreAuthorize("hasAuthority('教师')")
    @PostMapping("getTeacherInfo/{teacherNo}")
    public Result getTeacherInfo(@PathVariable Long teacherNo){
        if (teacherNo != null){
            return new Result(200,"获取教师信息成功！",teacherService.getOne(new QueryWrapper<Teacher>().eq("no",teacherNo)));
        }
        return new Result(201,"获取教师信息失败！");
    }

    /**
    * @description 绑定教师
    * @author Administrator
    * @date  11:43
    */
    @PreAuthorize("hasAuthority('教师')")
    @GetMapping("bindTeacher/{userId}/{teacherNo}")
    public Result bindTeacher(@PathVariable Long userId,@PathVariable Long teacherNo){
        return teacherService.bindTeacher(userId,teacherNo);
    }

    /**
    * @description 解绑教师
    * @author Administrator
    * @date  22:56
    */
    @PreAuthorize("hasAuthority('教师')")
    @GetMapping("unbindTeacher/{userId}")
    public Result unbindTeacher(@PathVariable Integer userId){
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
    * @description 根据教工号获取监考信息列表
    * @author Administrator
    * @date  14:55
    */
    @PreAuthorize("hasAuthority('教师')")
    @GetMapping("getTeacherExams/{teacherNo}")
    public Result getTeacherExams(@PathVariable Long teacherNo){
        return teacherService.getTeacherExams(teacherNo);
    }

    /**
    * @description 获取老师总数
    * @author Administrator
    * @date  14:43
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getTeachers")
    public Result getTeachers(){
        List<Teacher> teachers = teacherService.getBaseMapper().selectList(null);
        return Result.ok(teachers.size());
    }
}
