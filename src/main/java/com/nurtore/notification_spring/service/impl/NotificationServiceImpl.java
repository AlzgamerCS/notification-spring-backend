package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.*;
import com.nurtore.notification_spring.repository.NotificationRepository;
import com.nurtore.notification_spring.service.NotificationService;
import com.nurtore.notification_spring.service.NotificationPreferenceService;
import com.nurtore.notification_spring.service.EmailNotificationSender;
import com.nurtore.notification_spring.service.SmsNotificationSender;
import com.nurtore.notification_spring.service.TelegramNotificationSender;
import com.nurtore.notification_spring.service.InAppNotificationSender;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService preferenceService;
    private final EmailNotificationSender emailSender;
    private final SmsNotificationSender smsSender;
    private final TelegramNotificationSender telegramSender;
    private final InAppNotificationSender inAppSender;

    @Override
    public Notification createNotification(Notification notification) {
        notification.setStatus(NotificationStatus.PENDING);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification scheduleNotification(Document document, User user, NotificationType type, LocalDateTime scheduledAt) {
        Notification notification = new Notification();
        notification.setDocument(document);
        notification.setUser(user);
        notification.setType(type);
        notification.setScheduledAt(scheduledAt);
        notification.setStatus(NotificationStatus.PENDING);
        // Default to in-app notification
        notification.setChannel(NotificationChannel.IN_APP);
        
        return notificationRepository.save(notification);
    }

    @Override
    public Notification updateNotification(Notification notification) {
        if (!notificationRepository.existsById(notification.getId())) {
            throw new EntityNotFoundException("Notification not found with id: " + notification.getId());
        }
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> getNotificationById(UUID id) {
        return notificationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByDocument(Document document) {
        return notificationRepository.findByDocument(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getPendingNotificationsDue() {
        return notificationRepository.findPendingNotificationsDue(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotificationsByStatus(User user, NotificationStatus status) {
        return notificationRepository.findByUserAndStatusOrderByScheduledAtDesc(user, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotificationsInDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return notificationRepository.findUserNotificationsInDateRange(user, startDate, endDate);
    }

    @Override
    public void deleteNotification(UUID id) {
        if (!notificationRepository.existsById(id)) {
            throw new EntityNotFoundException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    @Override
    public Notification markNotificationAsSent(UUID id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public Notification markNotificationAsDismissed(UUID id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));
        notification.setStatus(NotificationStatus.DISMISSED);
        return notificationRepository.save(notification);
    }

    @Override
    @Scheduled(initialDelay = 10000, fixedRate = 15000) // Start 10 seconds after startup, then run every 15 seconds
    public void processNotifications() {
        log.info("Running scheduled notification processing at: {}", LocalDateTime.now());
        List<Notification> pendingNotifications = getPendingNotificationsDue();
        
        if (pendingNotifications.isEmpty()) {
            log.info("No pending notifications found to process");
            return;
        }
        
        log.info("Found {} pending notifications to process", pendingNotifications.size());
        
        for (Notification notification : pendingNotifications) {
            try {
                // Check if user has enabled this notification channel
                Optional<NotificationPreference> preference = 
                    preferenceService.getPreferenceByUserAndChannel(notification.getUser(), notification.getChannel());
                
                if (preference.isEmpty() || !preference.get().getEnabled()) {
                    log.info("Skipping notification {} as channel {} is disabled for user {}", 
                        notification.getId(), notification.getChannel(), notification.getUser().getId());
                    continue;
                }

                // Send notification based on channel
                boolean sent = switch (notification.getChannel()) {
                    case EMAIL -> emailSender.send(notification);
                    case SMS -> smsSender.send(notification);
                    case TELEGRAM -> telegramSender.send(notification);
                    case IN_APP -> inAppSender.send(notification);
                };

                if (sent) {
                    markNotificationAsSent(notification.getId());
                    log.info("Successfully sent notification: {}", notification.getId());
                } else {
                    throw new RuntimeException("Failed to send notification");
                }
            } catch (Exception e) {
                log.error("Failed to process notification: {}", notification.getId(), e);
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
            }
        }
        
        log.info("Completed notification processing. Processed {} notifications", pendingNotifications.size());
    }
} 