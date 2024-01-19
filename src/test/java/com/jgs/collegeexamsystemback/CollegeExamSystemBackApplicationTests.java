package com.jgs.collegeexamsystemback;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.body.RequestBody;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jgs.collegeexamsystemback.service.*;
import com.jgs.collegeexamsystemback.util.ChatGPTUtil;
import com.xkcoding.http.support.okhttp3.OkHttp3Impl;
import lombok.val;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

@SpringBootTest
class CollegeExamSystemBackApplicationTests {
    @Resource
    private ExamService examService;
    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void getAdmin(){
        String admin = passwordEncoder.encode("admin");
        System.out.println(admin);
    }

    @Test
    void textExam(){
        examService.schedulerExams();
    }


    @Test
    void testChatGpt() {

        HttpRequest request = HttpRequest.post("https://api.chatanywhere.com.cn/v1/chat/completions")
                .body("{\n \"model\": \"gpt-3.5-turbo\",\n \"messages\":[{\"role\":\"user\",\"content\":\"你是谁？\"}]}\n")
                .header("Authorization", "Bearer sk-WQUA5IXd5xjZ5T9khkCBAp4FUE42yRz6DSHm2dQo6LJQYjjL")
                .header("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .header("Content-Type", "application/json");
        HttpResponse response = request.execute();
        JSONObject parseObj = JSONUtil.parseObj(response.body());
        Object choices = parseObj.get("choices");
        JSONArray jsonArray = JSONUtil.parseArray(choices);
        JSONObject entries = JSONUtil.parseObj(jsonArray.get(0));
        String[] strings = entries.toString().split(",");
        String[] strings1 = strings[2].split(":");
        System.out.println(strings1[1].replace("}","").replace("\"",""));
    }

    @Test
    void testChatGPTUtil() throws IOException {
        String response = new ChatGPTUtil().getResponse("用java实现一个冒泡排序");
        System.out.println(response);
    }
}
