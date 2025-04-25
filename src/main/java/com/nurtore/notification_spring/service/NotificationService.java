package com.nurtore.notification_spring.service;

import com.nurtore.notification_spring.model.*;
import com.nurtore.notification_spring.dto.CalendarEventDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationService {
    Notification createNotification(Notification notification);
    Notification createNotificationWithEvent(Notification notification, CalendarEventDetails calendarEventDetails);
    Notification scheduleNotification(Document document, User user, NotificationType type, LocalDateTime scheduledAt);
    Notification updateNotification(Notification notification);
    Optional<Notification> getNotificationById(UUID id);
    List<Notification> getNotificationsByUser(User user);
    List<Notification> getNotificationsByDocument(Document document);
    List<Notification> getNotificationsByStatus(NotificationStatus status);
    List<Notification> getPendingNotificationsDue();
    List<Notification> getUserNotificationsByStatus(User user, NotificationStatus status);
    List<Notification> getUserNotificationsInDateRange(User user, LocalDateTime startDate, LocalDateTime endDate);
    void deleteNotification(UUID id);
    Notification markNotificationAsSent(UUID id);
    Notification markNotificationAsDismissed(UUID id);
    void processNotifications(); // Method to process pending notifications
} 