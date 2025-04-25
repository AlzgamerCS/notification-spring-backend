package com.nurtore.notification_spring.dto;

import com.nurtore.notification_spring.model.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationWithDocumentDTO {
    private UUID id;
    private UUID userId;
    private UUID documentId;
    private String documentTitle;
    private DocumentStatus documentStatus;
    private NotificationChannel channel;
    private NotificationType type;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private NotificationStatus status;

    public static NotificationWithDocumentDTO fromEntity(Notification notification) {
        NotificationWithDocumentDTO dto = new NotificationWithDocumentDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setDocumentId(notification.getDocument().getId());
        dto.setDocumentTitle(notification.getDocument().getTitle());
        dto.setDocumentStatus(notification.getDocument().getStatus());
        dto.setChannel(notification.getChannel());
        dto.setType(notification.getType());
        dto.setScheduledAt(notification.getScheduledAt());
        dto.setSentAt(notification.getSentAt());
        dto.setStatus(notification.getStatus());
        return dto;
    }
} 