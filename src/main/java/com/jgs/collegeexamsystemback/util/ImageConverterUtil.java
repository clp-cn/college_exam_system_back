package com.jgs.collegeexamsystemback.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 * @description 图片生成工具
 * @date 2023/7/25 0025 22:08
 */
public class ImageConverterUtil {
    public void generateImage(String text, String outputPath) {
        int width = 100; // 图片宽度
        int height = 100; // 图片高度

        // 创建一个 BufferedImage 对象作为绘图容器
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 获取 Graphics2D 对象，用于绘制图像
        Graphics2D g2d = image.createGraphics();

        // 设置背景色和绘图区域
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // 设置字体和颜色
        Font font = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        // 在指定位置绘制文本
        int x = 50;
        int y = 50;
        g2d.drawString(text, x, y);

        // 释放绘图资源
        g2d.dispose();

        try {
            // 将生成的图像保存为文件
            ImageIO.write(image, "png", new File(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
