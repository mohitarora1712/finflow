package com.finflow.document.storage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

 private final String basePath = "uploads/";

 public String store(UUID appId, MultipartFile file) {

  try {
   String dir = basePath + appId;
   Files.createDirectories(Paths.get(dir));

   String filePath = dir + "/" + file.getOriginalFilename();

   Files.copy(file.getInputStream(), Paths.get(filePath));

   return filePath;

  } catch (Exception e) {
   throw new RuntimeException("File storage failed");
  }
 }
}
