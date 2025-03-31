package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.FileMetadata;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FileMetadataDTO {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadDateTime;
    private UserDTO uploadedBy;

    @Data
    @Builder
    public static class UserDTO {
        private UUID id;
        private String name;
        private String email;
    }

    public static FileMetadataDTO fromEntity(FileMetadata entity) {
        return FileMetadataDTO.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .originalFileName(entity.getOriginalFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .uploadDateTime(entity.getUploadDateTime())
                .uploadedBy(entity.getUploadedBy() != null ? UserDTO.builder()
                        .id(entity.getUploadedBy().getId())
                        .name(entity.getUploadedBy().getName())
                        .email(entity.getUploadedBy().getEmail())
                        .build() : null)
                .build();
    }
} 