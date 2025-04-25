package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.DocumentCategory;
import com.nurtore.notification_spring.model.DocumentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateDocumentWithEventRequest {
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
    
    @Valid
    @NotNull(message = "Calendar event details are required")
    private CalendarEventDetails calendarEventDetails;
} 