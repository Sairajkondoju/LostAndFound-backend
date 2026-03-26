package com.campuslostfound.service;

import com.campuslostfound.exception.BadRequestException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        try {
            Path directory = Path.of(uploadDir);
            Files.createDirectories(directory);
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), directory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return "/api/uploads/" + fileName;
        } catch (IOException ex) {
            throw new BadRequestException("Could not store file");
        }
    }
}
