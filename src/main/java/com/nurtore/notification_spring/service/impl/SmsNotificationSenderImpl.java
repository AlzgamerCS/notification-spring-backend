package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.Notification;
import com.nurtore.notification_spring.service.SmsNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationSenderImpl implements SmsNotificationSender {
    @Override
    public boolean send(Notification notification) {
        // TODO: Implement actual SMS sending logic
        log.info("Sending SMS notification to user: {}", notification.getUser().getId());
        return true;
    }
} 