package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.NotificationChannel;
import com.nurtore.notification_spring.model.NotificationStatus;
import com.nurtore.notification_spring.model.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationDTO {
    private UUID id;
    private UUID userId;
    private UUID documentId;
    private NotificationChannel channel;
    private NotificationType type;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private NotificationStatus status;

    // Static factory method to create DTO from entity
    public static NotificationDTO fromEntity(com.nurtore.notification_spring.model.Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setDocumentId(notification.getDocument().getId());
        dto.setChannel(notification.getChannel());
        dto.setType(notification.getType());
        dto.setScheduledAt(notification.getScheduledAt());
        dto.setSentAt(notification.getSentAt());
        dto.setStatus(notification.getStatus());
        return dto;
    }
} 