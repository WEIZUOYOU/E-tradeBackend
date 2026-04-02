package com.campus.trade.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUploadUtils {

    /**
     * 保存文件到本地目录
     * 
     * @param baseDir 基础目录（如 /opt/uploads/）
     * @param file    上传的文件
     * @return 返回相对路径（如 /uploads/xxx.jpg），用于访问
     */

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public static String saveFile(String baseDir, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("文件为空");
        }
        // 限制大小
        if (file.getSize() > MAX_SIZE) {
            throw new IOException("文件不能超过5MB");
        }

        // 限制类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                !(originalFilename.endsWith(".jpg") ||
                        originalFilename.endsWith(".png") ||
                        originalFilename.endsWith(".jpeg"))) {
            throw new IOException("只支持jpg/png/jpeg格式");
        }

        // 获取原始文件名，生成唯一名称
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 按日期分目录存储
        String dateDir = java.time.LocalDate.now().toString();
        Path uploadPath = Paths.get(baseDir, dateDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path targetPath = uploadPath.resolve(newFileName);
        file.transferTo(targetPath.toFile());

        // 返回访问路径
        return "/uploads/" + dateDir + "/" + newFileName;
    }
}