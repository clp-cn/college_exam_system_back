package com.jgs.collegeexamsystemback.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.College;
import com.jgs.collegeexamsystemback.pojo.ExamClass;
import com.jgs.collegeexamsystemback.pojo.Profession;
import com.jgs.collegeexamsystemback.pojo.ProfessionClass;
import com.jgs.collegeexamsystemback.service.CollegeService;
import com.jgs.collegeexamsystemback.service.ExamClassService;
import com.jgs.collegeexamsystemback.service.ProfessionService;
import com.jgs.collegeexamsystemback.service.ProfessionClassService;
import com.jgs.collegeexamsystemback.vo.AddClassVo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.*;

/**
 * @author Administrator
 * @version 1.0
 * @description 学校控制类
 * @date 2023/8/13 0013 17:33
 */
@RestController
@RequestMapping("school")
public class SchoolController {
    @Resource
    private CollegeService collegeService;
    @Resource
    private ProfessionService professionService;
    @Resource
    private ExamClassService examClassService;
    @Resource
    private ProfessionClassService professionClassService;
    @Resource
    private RedisCache redisCache;

    /**
    * @description 获取学院分页列表
    * @author Administrator
    * @date  17:35
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getCollegeList/{pageNo}/{pageSize}")
    public Result getCollegeList(@PathVariable Integer pageNo,@PathVariable Integer pageSize){
        // 从redis中查询
        String redisKey = pageNo + "_" + pageSize;
        Page<College> collegePage = redisCache.getCacheMapValue("college", redisKey);
        if (collegePage == null){
            // redis中不存在，从mysql中查询
            Page<College> page = new Page<>(pageNo,pageSize);
            Page<College> collegePage1 = collegeService.getBaseMapper().selectPage(page, null);
            collegePage1.getRecords().sort(Comparator.comparing(College::getCreateTime));
            redisCache.setCacheMapValue("college",redisKey,collegePage1);
            return Result.ok(collegePage1);
        }
        return Result.ok(collegePage);
    }

    /**
     * @description 获取学院列表
     * @author Administrator
     * @date 14:26
     */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getColleges")
    public Result getColleges() {
        List<College> collegeList = collegeService.getBaseMapper().selectList(null);
        List<String> colleges = new ArrayList<>();
        collegeList.forEach(college -> colleges.add(college.getCollege()));
        return Result.ok(colleges);
    }

    /**
    * @description 删除学院
    * @author Administrator
    * @date  18:17
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteCollege/{collegeId}")
    public Result deleteCollege(@PathVariable Integer collegeId){
        if (collegeId != null){
            boolean result = collegeService.removeById(collegeId);
            redisCache.deleteObject("college");
            if (result){
                return Result.ok();
            }
        }
        return Result.fail();
    }

    /**
    * @description 批量删除学院
    * @author Administrator
    * @date  18:25
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteColleges")
    public Result deleteColleges(@RequestBody List<Integer> collegeIds){
        if (collegeIds != null && !collegeIds.isEmpty()){
            boolean result = collegeService.removeBatchByIds(collegeIds);
            redisCache.deleteObject("college");
            if (result){
                return Result.ok();
            }
        }
        return Result.fail();
    }

    /**
    * @description 新增学院
    * @author Administrator
    * @date  18:33
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("addCollege")
    public Result addCollege(@RequestBody College college){
        if (college != null){
            // 查询数据库是否已存在该数据
            College selectCollege = collegeService.getOne(new QueryWrapper<College>().eq("college", college.getCollege()));
            if (selectCollege != null){
                return new Result(201,"添加失败，学院重复！");
            }
            college.setCreateTime(new Date());
            college.setUpdateTime(college.getCreateTime());
            boolean result = collegeService.save(college);
            redisCache.deleteObject("college");
            if (result) {
                return new Result(200,"添加成功！");
            }
        }
        return new Result(201,"添加失败！");
    }

    /**
    * @description 更新学院
    * @author Administrator
    * @date  18:50
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("updateCollege")
    public Result updateCollege(@RequestBody College college){
        if (college != null){
            college.setUpdateTime(new Date());
            boolean result = collegeService.updateById(college);
            redisCache.deleteObject("college");
            if (result){
                return Result.ok();
            }
        }
        return Result.fail();
    }

    /**
    * @description 获取专业分页列表
    * @author Administrator
    * @date  17:48
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getProfessionList/{pageNo}/{pageSize}")
    public Result getProfessionList(@PathVariable Integer pageNo,@PathVariable Integer pageSize){
        String redisKey = pageNo + "_" + pageSize;
        Page<Profession> professionPage = redisCache.getCacheMapValue("profession", redisKey);
        if (professionPage == null){
            Page<Profession> page = new Page<>(pageNo,pageSize);
            Page<Profession> professionPage1 = professionService.getBaseMapper().selectPage(page, null);
            professionPage1.getRecords().sort(Comparator.comparing(Profession::getCreateTime));
            redisCache.setCacheMapValue("profession",redisKey,professionPage1);
            return Result.ok(professionPage1);
        }
        return Result.ok(professionPage);
    }

    /**
     * @description 根据学院获取专业列表
     * @author Administrator
     * @date 14:45
     */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getProfessions")
    public Result getProfessions(@RequestParam String college) {
        return professionService.getProfessionsByCollege(college);
    }

    /**
    * @description 新增专业
    * @author Administrator
    * @date  19:54
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("addProfession")
    public Result addProfession(@RequestBody Profession profession){
        if (profession != null){
            Profession selectProfession = professionService.getOne(new QueryWrapper<Profession>().eq("profession", profession.getProfession()));
            if (selectProfession != null){
                return new Result(201,"添加失败，专业重复！");
            }
            profession.setCreateTime(new Date());
            boolean result = professionService.save(profession);
            redisCache.deleteObject("profession");
            if (result){
                return Result.ok();
            }
        }
        return new Result(201,"添加失败！");
    }

    /**
    * @description 删除专业
    * @author Administrator
    * @date  19:49
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteProfession/{professionId}")
    public Result deleteProfession(@PathVariable Integer professionId){
        if (professionId != null){
            boolean result = professionService.removeById(professionId);
            redisCache.deleteObject("profession");
            if (result){
                return Result.ok();
            }
        }
        return Result.fail();
    }

    /**
    * @description 批量删除专业
    * @author Administrator
    * @date  19:51
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteProfessions")
    public Result deleteProfessions(@RequestBody List<Integer> professionIds){
        if (professionIds != null && !professionIds.isEmpty()){
            boolean result = professionService.removeBatchByIds(professionIds);
            redisCache.deleteObject("profession");
            if (result){
                return Result.ok();
            }
        }
        return Result.fail();
    }

    /**
    * @description 获取班级分页列表
    * @author Administrator
    * @date  21:08
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getClassList/{pageNo}/{pageSize}")
    public Result getClassList(@PathVariable Integer pageNo,@PathVariable Integer pageSize){
        String redisKey = pageNo + "_" + pageSize;
        Page<ExamClass> classesPage = redisCache.getCacheMapValue("classes", redisKey);
        if (classesPage == null){
            Page<ExamClass> page = new Page<>(pageNo,pageSize);
            Page<ExamClass> examClassPage = examClassService.getBaseMapper().selectPage(page, null);
            examClassPage.getRecords().sort(Comparator.comparing(ExamClass::getCreateTime));
            redisCache.setCacheMapValue("classes",redisKey,examClassPage);
            return Result.ok(examClassPage);
        }
        return Result.ok(classesPage);
    }

    /**
    * @description 获取所有的班级
    * @author Administrator
    * @date  10:24
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getClasses")
    public Result getClasses(){
        List<ExamClass> examClassList = examClassService.getAll();
        List<String> classes = new ArrayList<>();
        examClassList.forEach(examClass -> classes.add(examClass.getName()));
        return Result.ok(classes);
    }

    /**
    * @description 根据专业和年级获取班级列表
    * @author Administrator
    * @date  17:14
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getClassesBySearch")
    public Result getClassesByProAndGrade(@RequestParam String college,@RequestParam String profession,@RequestParam String grade){
        List<ProfessionClass> classes = professionClassService.getBaseMapper().selectList(new QueryWrapper<ProfessionClass>().like("college",college).like("profession", profession).like("grade", grade));
        List<String> classNames = new ArrayList<>();
        classes.forEach(c -> classNames.add(c.getName()));
        return Result.ok(classNames);
    }

    /**
    * @description 新增考试班级
    * @author Administrator
    * @date  21:26
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("addClass")
    public Result addClass(@RequestBody AddClassVo addClassVo){
        return examClassService.addClass(addClassVo);
    }

    /**
    * @description 删除班级
    * @author Administrator
    * @date  21:36
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteClass/{classId}")
    public Result deleteClass(@PathVariable Integer classId){
        return examClassService.deleteClass(classId);
    }

    /**
    * @description 批量删除考试班级
    * @author Administrator
    * @date  21:38
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteClasses")
    public Result deleteClasses(@RequestBody List<Integer> classIds){
        return examClassService.deleteClasses(classIds);
    }

    /**
    * @description 根据学院和年级获取公共班级列表
    * @author Administrator
    * @date  13:24
    */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("getElectiveClasses")
    public Result getElectiveClasses(@RequestParam String college,@RequestParam String grade){
        QueryWrapper<ExamClass> queryWrapper = new QueryWrapper<ExamClass>().like("college", college).like("grade", grade).eq("type","公共班级");
        List<ExamClass> classes = examClassService.getBaseMapper().selectList(queryWrapper);
        List<String> classNames = new ArrayList<>();
        classes.forEach(c -> classNames.add(c.getName()));
        return Result.ok(classNames);
    }
}
