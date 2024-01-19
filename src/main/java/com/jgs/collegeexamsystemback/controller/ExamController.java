package com.jgs.collegeexamsystemback.controller;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.*;
import com.jgs.collegeexamsystemback.service.*;
import com.jgs.collegeexamsystemback.vo.ExamSubjectVo;
import com.jgs.collegeexamsystemback.vo.UpdateSubjectVo;
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
 * @description ExamController
 * @date 2023/7/19 0019 20:20
 */
@RestController
@RequestMapping("exam")
public class ExamController {
    @Resource
    private ExamService examService;
    @Resource
    private ExamSubjectService examSubjectService;
    @Resource
    private SubjectClassService subjectClassService;
    @Resource
    private StudentService studentService;
    @Resource
    private ExamDateService examDateService;
    @Resource
    private ExamCardService examCardService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ExamClassService examClassService;

    /**
    * @description 自动排考
    * @author Administrator
    * @date  20:24
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("schedulerExams/{start}/{end}")
    public Result schedulerExams(@PathVariable Date start,@PathVariable Date end){
        List<Date> times = new ArrayList<>();
        int days = (int) ((end.getTime()-start.getTime())/24/60/60/1000);
        for(int i = 0;i <= days;i++){
            Date t1 = new Date(start.getTime() + 17*60*60*1000);
            times.add(t1);
            Date t2 = new Date(start.getTime() + 23*60*60*1000);
            times.add(t2);
            start = new Date(start.getTime() + (long) 24 * 60 * 60 * 1000);
        }
        // 查询考试天数和班级科目数的冲突关系（一天只允许一个班级考一门）
        boolean isClash = examService.isClash(days + 1);
        if (isClash){
            return new Result(201,"排考失败，一个班级一天只能安排一场考试，考试天数不足，请重新安排！");
        }
        // 清空原时间段
        examDateService.remove(null);
        // 清空原考试安排表
        examService.remove(null);
        // 清空原准考证表
        examCardService.remove(null);
        redisCache.deleteObject("exam");
        redisCache.deleteObject("examCard");
        times.forEach(t -> {
            ExamDate examDate = new ExamDate();
            examDate.setTime(t);
            examDateService.save(examDate);
        });
        examService.schedulerExams();
        List<Student> students = studentService.getBaseMapper().selectList(null);
        List<ExamCard> examCards = new ArrayList<>();
        students.forEach(student -> {
            ExamCard examCard = new ExamCard();
            examCard.setNo(student.getNo());
            examCard.setName(student.getName());
            examCard.setSex(student.getSex());
            examCard.setClassName(student.getClassName());
            examCards.add(examCard);
        });
        examCardService.saveBatch(examCards);
        return new Result(200,"排考成功！");
    }

    /**
    * @description 获取排考分页列表
    * @author Administrator
    * @date  20:26
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("getExamList/{pageNo}/{pageSize}")
    public Result getExamList(@PathVariable Integer pageNo, @PathVariable Integer pageSize){
        return examService.getExamList(pageNo,pageSize);
    }

    /**
    * @description 获取考试科目分页列表
    * @author Administrator
    * @date  20:29
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getSubjectList/{pageNo}/{pageSize}")
    public Result getSubjectList(@PathVariable Integer pageNo,@PathVariable Integer pageSize){
        return examSubjectService.getSubjectPageList(pageNo,pageSize);
    }

    /**
    * @description 添加考试科目
    * @author Administrator
    * @date  20:38
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("saveSubject")
    public Result saveSubject(@RequestBody ExamSubjectVo examSubjectVo){
        return examSubjectService.saveSubject(examSubjectVo);
    }

    /**
     * 更新科目信息
     * @return
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("updateSubject")
    public Result updateSubject(@RequestBody UpdateSubjectVo updateSubjectVo){
        return examSubjectService.updateSubject(updateSubjectVo);
    }

    /**
    * @description 删除科目
    * @author Administrator
    * @date  20:42
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteSubject/{subjectId}")
    public Result deleteSubject(@PathVariable Integer subjectId){
        return subjectClassService.deleteSubjectClass(subjectId);
    }

    /**
    * @description 批量删除科目
    * @author Administrator
    * @date  20:45
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteSubjects/{subjectIds}")
    public Result deleteSubjects(@PathVariable List<Integer> subjectIds){
        return subjectClassService.deleteSubjectClasses(subjectIds);
    }

    /**
     * 接收examExcel文件，批量插入考试安排数据
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping( "receiveExcel")
    public Result uploadExcel(@RequestParam("file")MultipartFile file){
        try {
            Workbook sheets = WorkbookFactory.create(file.getInputStream());
            if (sheets != null){
                List<List<Object>> lists = ExcelUtil.getReader(file.getInputStream()).read(2);
                sheets.close();
                return examService.saveExams(lists);
            }
            return new Result(201,"接收失败！");
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(201,"文件格式错误！");
        }
    }

    /**
    * @description 获取所有的考试安排
    * @author Administrator
    * @date  10:00
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getExams")
    public Result getExams(){
        List<Exam> exams = examService.getBaseMapper().selectList(null);
        exams.sort(Comparator.comparing(Exam::getExamStart));
        exams.forEach(exam -> {
            exam.setExamStart(new Date(exam.getExamStart().getTime() - 8*60*60*1000));
            exam.setExamEnd(new Date(exam.getExamEnd().getTime() - 8*60*60*1000));
        });
        return Result.ok(exams);
    }

    /**
    * @description 删除所有的考试安排
    * @author Administrator
    * @date  9:59
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteAll")
    public Result deleteAll(){
        return examService.deleteAll();
    }

    /**
    * @description 获取准考证分页列表
    * @author Administrator
    * @date  9:59
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getExamCardList/{pageNo}/{pageSize}")
    public Result getExamCardList(@PathVariable Integer pageNo,@PathVariable Integer pageSize,@RequestParam String query){
        String redisKey = query + "_" + pageNo + "_" + pageSize;
        Page<ExamCard> examCardPage = redisCache.getCacheMapValue("examCard",redisKey);
        if (examCardPage == null){
            Page<ExamCard> page = new Page<>(pageNo,pageSize);
            QueryWrapper<ExamCard> wrapper = new QueryWrapper<>();
            if (query == null){
                wrapper.orderByAsc("no");
            }else {
                wrapper.like("class_name",query).orderByAsc("no");
            }
            Page<ExamCard> examCardPage1 = examCardService.getBaseMapper().selectPage(page, wrapper);
            redisCache.setCacheMapValue("examCard",redisKey,examCardPage1);
            return Result.ok(examCardPage1);
        }
        return Result.ok(examCardPage);
    }

    /**
     * 接收examCardExcel文件 批量插入准考证号数据
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("receiveExamCardExcel")
    public Result receiveExamCardExcel(@RequestBody MultipartFile file){
        try {
            Workbook sheets = WorkbookFactory.create(file.getInputStream());
            if (sheets != null){
                List<List<Object>> lists = ExcelUtil.getReader(file.getInputStream()).read(2);
                sheets.close();
                return examCardService.saveExamCards(lists);
            }
            return new Result(201,"接收失败！");
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(201,"文件格式错误！");
        }
    }

    /**
    * @description 获取所有的准考证
    * @author Administrator
    * @date  11:43
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getExamCards")
    public Result getExamCards(){
        List<ExamCard> examCards = examCardService.getBaseMapper().selectList(null);
        return Result.ok(examCards);
    }

    /**
     * 删除所有的准考证
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteExamCardAll")
    public Result deleteExamCardAll(){
        return examCardService.deleteExamCardAll();
    }
}
