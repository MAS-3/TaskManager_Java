package com.example.taskmanager;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 画像のアップロード設定
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "http://localhost:8080/uploads/..." へのアクセスを
        // サーバー内の "/data/uploads/" フォルダへのアクセスに割り当てる設定
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/data/uploads/");
    }
}