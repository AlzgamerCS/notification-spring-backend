package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.dto.FileMetadataDTO;
import com.nurtore.notification_spring.model.FileMetadata;
import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<List<FileMetadataDTO>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal User user) {
        
        List<FileMetadata> uploadedFiles = fileStorageService.storeFiles(files, user);
        List<FileMetadataDTO> dtos = uploadedFiles.stream()
                .map(FileMetadataDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileName") String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
} 