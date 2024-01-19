package com.jgs.collegeexamsystemback.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.stereotype.Component;


/**
 * ChatGPTUtil
 */
@Component
public class ChatGPTUtil {
    private static final String url = "https://api.chatanywhere.com.cn/v1/chat/completions";
    public String getResponse(String question){
        HttpRequest request = HttpRequest.post(url)
                .body("{\n \"model\": \"gpt-3.5-turbo\",\n \"messages\":[{\"role\":\"user\",\"content\":\""+question+"\"}]}\n")
                .header("Authorization", "Bearer sk-WQUA5IXd5xjZ5T9khkCBAp4FUE42yRz6DSHm2dQo6LJQYjjL")
                .header("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .header("Content-Type", "application/json");
        HttpResponse response = request.execute();
//        JSONObject parseObj = JSONUtil.parseObj(response.body());
//        Object choices = parseObj.get("choices");
//        JSONArray jsonArray = JSONUtil.parseArray(choices);
//        JSONObject entries = JSONUtil.parseObj(jsonArray.get(0));
//        String[] strings = entries.toString().split(",");
//        String[] strings1 = strings[2].split(":");
//        return strings1[1].replace("}","").replace("\"","");
        return response.body();
    }
}
