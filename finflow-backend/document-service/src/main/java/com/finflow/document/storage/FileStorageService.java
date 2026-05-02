package com.finflow.document.storage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final String basePath = "uploads/";

    public String store(UUID appId, MultipartFile file) {

        try {
            // NEW CHANGE: Sanitize and prevent path traversal
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Cannot store file with relative path outside current directory");
            }

            // NEW CHANGE: Generate UUID-based filename to prevent collision and preserve extension
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String storedFilename = UUID.randomUUID().toString() + extension;

            String dir = basePath + appId;
            Files.createDirectories(Paths.get(dir));

            String filePath = dir + "/" + storedFilename;

            Files.copy(file.getInputStream(), Paths.get(filePath));

            log.info("File stored successfully: original={} stored={}", originalFilename, storedFilename);
            return filePath;

        } catch (Exception e) {
            log.error("File storage failed for appId={}: {}", appId, e.getMessage());
            throw new RuntimeException("File storage failed");
        }
    }
}
