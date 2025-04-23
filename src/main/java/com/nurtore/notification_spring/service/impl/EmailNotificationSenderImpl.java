package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.Notification;
import com.nurtore.notification_spring.service.EmailNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationSenderImpl implements EmailNotificationSender {
    @Override
    public boolean send(Notification notification) {
        // TODO: Implement actual email sending logic
        log.info("Sending email notification to user: {}", notification.getUser().getId());
        return true;
    }
} 