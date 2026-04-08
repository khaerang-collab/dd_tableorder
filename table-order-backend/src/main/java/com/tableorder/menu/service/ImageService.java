package com.tableorder.menu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Value("${app.upload.path}") private String uploadPath;

    public String upload(MultipartFile file) throws IOException {
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException("JPG, PNG 파일만 업로드 가능합니다");
        if (file.getSize() > MAX_SIZE)
            throw new IllegalArgumentException("파일 크기는 5MB 이하만 가능합니다");

        String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadPath);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename));
        return "/api/images/" + filename;
    }
}
