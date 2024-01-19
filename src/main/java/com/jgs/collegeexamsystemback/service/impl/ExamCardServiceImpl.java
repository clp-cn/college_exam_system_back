package com.jgs.collegeexamsystemback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgs.collegeexamsystemback.dto.RedisCache;
import com.jgs.collegeexamsystemback.dto.Result;
import com.jgs.collegeexamsystemback.mapper.ExamCardMapper;
import com.jgs.collegeexamsystemback.pojo.ExamCard;
import com.jgs.collegeexamsystemback.service.ExamCardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @description ExamCardServiceImpl
 * @date 2023/8/14 0014 11:27
 */
@Service
@Transactional
public class ExamCardServiceImpl extends ServiceImpl<ExamCardMapper, ExamCard> implements ExamCardService {
    @Resource
    private RedisCache redisCache;

    // 接收excel文件 批量插入准考证号数据
    @Override
    public Result saveExamCards(List<List<Object>> lists) {
        List<ExamCard> examCards = new ArrayList<>();
        lists.forEach(data -> {
            ExamCard examCard = new ExamCard();
            examCard.setNo(Integer.valueOf(data.get(1).toString()));
            examCard.setName(data.get(2).toString());
            examCard.setSex(data.get(3).toString());
            examCard.setClassName(data.get(4).toString());
            ExamCard one = baseMapper.selectOne(new QueryWrapper<ExamCard>().eq("no", examCard.getNo()));
            if (one == null){
                examCards.add(examCard);
            }
        });
        if (examCards.isEmpty()){
            return new Result(201,"导入失败，文件内容已存在!");
        }
        boolean result = saveBatch(examCards,examCards.size());
        if (result){
            redisCache.deleteObject("examCard");
            return new Result(200,"导入成功!");
        }
        return new Result(201,"导入失败，请重试!");
    }

    // 删除所有的准考证号
    @Override
    public Result deleteExamCardAll() {
        int delete = baseMapper.delete(null);
        if (delete > 0){
            redisCache.deleteObject("examCard");
            return new Result(200,"删除成功！");
        }
        return new Result(201,"删除失败！");
    }
}
