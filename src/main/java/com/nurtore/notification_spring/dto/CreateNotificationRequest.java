package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.NotificationChannel;
import com.nurtore.notification_spring.model.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateNotificationRequest {
    @NotNull
    private UUID documentId;
    
    @NotNull
    private NotificationChannel channel;
    
    @NotNull
    private NotificationType type;
    
    @NotNull
    private LocalDateTime scheduledAt;
} 