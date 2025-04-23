package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.Notification;
import com.nurtore.notification_spring.service.InAppNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InAppNotificationSenderImpl implements InAppNotificationSender {
    @Override
    public boolean send(Notification notification) {
        // TODO: Implement actual in-app notification logic
        log.info("Sending in-app notification to user: {}", notification.getUser().getId());
        return true;
    }
} 