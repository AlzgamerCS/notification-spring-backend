package com.nurtore.notification_spring.service;

import com.nurtore.notification_spring.exception.FileStorageException;
import com.nurtore.notification_spring.model.FileMetadata;
import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.repository.FileMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation;
    private final FileMetadataRepository fileMetadataRepository;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public List<FileMetadata> storeFiles(MultipartFile[] files, User user) {
        List<FileMetadata> storedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            storedFiles.add(storeFile(file, user));
        }
        
        return storedFiles;
    }

    public FileMetadata storeFile(MultipartFile file, User user) {
        String path = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file";
        String originalFileName = StringUtils.cleanPath(path);
        String fileExtension = originalFileName.lastIndexOf(".") > 0 ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence " + originalFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileMetadata metadata = FileMetadata.builder()
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .filePath(targetLocation.toString())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadDateTime(LocalDateTime.now())
                    .uploadedBy(user)
                    .build();

            return fileMetadataRepository.save(metadata);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileName, ex);
        }
    }
} 