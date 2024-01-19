package com.jgs.collegeexamsystemback.util;

import lombok.Data;

import java.util.Random;

/**
 * @author Administrator
 * @version 1.0
 * @description 生成随机密码工具类
 * @date 2023/7/24 0024 16:42
 */
@Data
public class GetTemPasswordUtil {
    public String getTemPassword(){
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int num = random.nextInt(10);
            stringBuilder.append(num);
        }
        return stringBuilder.toString();
    }
}
