package com.campus.trade.config;

import com.campus.trade.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 配置跨域支持 (CORS)
     * 解决前端无法访问后端接口的问题
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 允许携带 Cookie
                .exposedHeaders("Set-Cookie") // 暴露 Set-Cookie 头给前端
                .maxAge(3600);
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理 Windows 和 Linux 路径差异，统一转换为正斜杠格式
        String normalizedPath = uploadDir.replace("\\", "/");
        
        // 构建正确的 file: URI 格式
        String location;
        if (normalizedPath.startsWith("file:")) {
            location = normalizedPath;
        } else {
            // Windows: d:/path/to/uploads -> file:///d:/path/to/uploads
            // Linux: /path/to/uploads -> file:///path/to/uploads
            location = "file:///" + normalizedPath;
        }
        
        // 确保路径以 / 结尾
        if (!location.endsWith("/")) {
            location += "/";
        }
        
        System.out.println("\n========================================");
        System.out.println("=== 静态资源映射配置 ===");
        System.out.println("========================================");
        System.out.println("原始路径: " + uploadDir);
        System.out.println("标准化路径: " + normalizedPath);
        System.out.println("资源位置: " + location);
        System.out.println("资源映射: /uploads/** -> " + location);
        
        // 验证目录是否存在
        java.io.File uploadDirFile = new java.io.File(uploadDir);
        System.out.println("上传目录是否存在: " + uploadDirFile.exists());
        System.out.println("上传目录绝对路径: " + uploadDirFile.getAbsolutePath());
        
        if (uploadDirFile.exists()) {
            System.out.println("上传目录内容:");
            java.io.File[] files = uploadDirFile.listFiles();
            if (files != null && files.length > 0) {
                for (java.io.File file : files) {
                    System.out.println("  - " + file.getName() + (file.isDirectory() ? " [目录]" : " [文件]"));
                }
            } else {
                System.out.println("  (空目录)");
            }
        } else {
            System.out.println("⚠️ 警告: 上传目录不存在！");
        }
        System.out.println("========================================\n");
        
        // 添加静态资源映射（同时支持带和不带前导斜杠的路径）
        // Spring会自动处理两种格式，无需分别配置
        registry.addResourceHandler("/uploads/**", "uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(31536000); // 1年缓存
        
        System.out.println("✅ 静态资源映射配置完成");
        System.out.println("   - 支持: /uploads/** (标准路径)");
        System.out.println("   - 支持: uploads/** (兼容旧数据)");
    }

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/product/list",
                        "/api/product/detail/**",
                        "/api/category/list",
                        "/uploads/**"  // 排除静态资源路径
                );
    }
}