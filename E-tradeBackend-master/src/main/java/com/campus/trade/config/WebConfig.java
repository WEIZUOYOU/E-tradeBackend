// src/main/java/com/campus/trade/config/WebConfig.java
package com.campus.trade.config;

import com.campus.trade.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login", "/api/user/register", 
                    "/api/product/list", "/api/product/detail/**", "/upload/**",
                    "/api/category/**");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传目录为静态资源
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadDir);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")  // 前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}