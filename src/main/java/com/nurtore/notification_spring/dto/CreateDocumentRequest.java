package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.DocumentCategory;
import com.nurtore.notification_spring.model.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateDocumentRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private DocumentCategory category;
    
    private Set<String> tags;
    
    @NotNull
    private LocalDate expirationDate;
    
    private String filePath;
    
    private DocumentStatus status;
} 