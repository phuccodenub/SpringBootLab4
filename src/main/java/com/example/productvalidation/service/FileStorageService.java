package com.example.productvalidation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể tạo thư mục lưu ảnh: " + uploadPath, ex);
        }
    }

    public String save(MultipartFile file) {
        var originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
        var extension = extractExtension(originalFileName);
        var savedFileName = UUID.randomUUID().toString().replace("-", "") + extension;

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(savedFileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu hình ảnh", ex);
        }

        return savedFileName;
    }

    public void deleteIfExists(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(uploadPath.resolve(fileName));
        } catch (IOException ignored) {
        }
    }

    public Path getUploadPath() {
        return uploadPath;
    }

    private String sanitizeOriginalFileName(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }
        return Paths.get(originalFileName).getFileName().toString();
    }

    private String extractExtension(String fileName) {
        var index = fileName.lastIndexOf('.');
        return index >= 0 ? fileName.substring(index) : "";
    }
}
