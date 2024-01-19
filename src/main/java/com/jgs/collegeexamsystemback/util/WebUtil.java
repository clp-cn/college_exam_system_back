package com.jgs.collegeexamsystemback.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @description WebUtil
 * @date 2023/7/8 0008 21:13
 */
public class WebUtil {
    public static void renderString(HttpServletResponse response, String string){
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
