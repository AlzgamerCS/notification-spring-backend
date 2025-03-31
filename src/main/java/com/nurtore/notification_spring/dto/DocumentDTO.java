package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.Document;
import com.nurtore.notification_spring.model.DocumentCategory;
import com.nurtore.notification_spring.model.DocumentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class DocumentDTO {
    private UUID id;
    private String title;
    private String description;
    private DocumentCategory category;
    private Set<String> tags;
    private LocalDate expirationDate;
    private String filePath;
    private DocumentStatus status;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DocumentDTO fromEntity(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setDescription(document.getDescription());
        dto.setCategory(document.getCategory());
        dto.setTags(document.getTags());
        dto.setExpirationDate(document.getExpirationDate());
        dto.setFilePath(document.getFilePath());
        dto.setStatus(document.getStatus());
        dto.setOwnerId(document.getOwner().getId());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }
} 