package com.jgs.collegeexamsystemback.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.pojo.ExamRoom;
import com.jgs.collegeexamsystemback.service.ExamRoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @description 考场控制类
 * @date 2023/7/21 0021 22:14
 */
@RestController
@RequestMapping("examRoom")
public class ExamRoomController {
    @Resource
    private ExamRoomService examRoomService;
    @Resource
    private RedisCache redisCache;

    /**
    * @description 根据条件查询考场分页列表
    * @author Administrator
    * @date  22:17
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("getExamRoomList/{pageNo}/{pageSize}")
    public Result getExamRoomPageList(@PathVariable int pageNo, @PathVariable int pageSize, @RequestBody Map<String,String> query){
        String redisKey = query.get("teachBuilding") + "_" + pageNo + "_" + pageSize;
        Page<ExamRoom> examRoomPage = redisCache.getCacheMapValue("room",redisKey);
        if (examRoomPage == null){
            Page<ExamRoom> page = new Page<>(pageNo, pageSize);
            Page<ExamRoom> roomPageList = examRoomService.getExamRoomPageList(page,query);
            redisCache.setCacheMapValue("room",redisKey,roomPageList);
            return Result.ok(roomPageList);
        }
        return Result.ok(examRoomPage);
    }

    /**
    * @description 新增考场
    * @author Administrator
    * @date  22:38
    */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("saveExamRoom")
    public Result saveExamRoom(@RequestBody ExamRoom examRoom){
        if (examRoom == null){
            return new Result(201,"添加失败，考场不能为空！");
        }
        ExamRoom select = examRoomService.getOne(new QueryWrapper<ExamRoom>().eq("teach_building", examRoom.getTeachBuilding()).eq("name", examRoom.getName()));
        if (select != null){
            return new Result(201,"添加失败，不可重复添加！");
        }
        boolean result = examRoomService.save(examRoom);
        if (result){
            redisCache.deleteObject("room");
            return new Result(200,"添加成功！");
        }
        return new Result(201,"操作失败，请检查网络或重试！");
    }

    /**
    * @description 根据id删除考场
    * @author Administrator
    * @date  22:40
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteExamRoom/{id}")
    public Result deleteExamRoom(@PathVariable Long id){
        if (id != null){
            boolean result = examRoomService.removeById(id);
            if (result){
                redisCache.deleteObject("room");
                return new Result(200,"删除成功！");
            }
        }
        return new Result(201,"删除失败，请重试！");
    }

    /**
    * @description 批量删除考场
    * @author Administrator
    * @date  22:42
    */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("deleteExamRooms")
    public Result deleteExamRooms(@RequestBody List<ExamRoom> examRooms){
        if (examRooms != null && !examRooms.isEmpty()){
            List<Long> ids = new ArrayList<>();
            examRooms.forEach(examRoom -> ids.add(examRoom.getId()));
            boolean result = examRoomService.removeBatchByIds(ids);
            if (result){
                redisCache.deleteObject("room");
                return new Result(200,"删除成功！");
            }
        }
        return new Result(201,"删除失败，请重试！");
    }
}
