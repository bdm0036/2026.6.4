package com.bookstore.product.controller;

import com.bookstore.common.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @SuppressWarnings("unchecked")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            String url = "/uploads/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", filename);
            log.info("文件上传成功: {}", filename);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return (Result) Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
