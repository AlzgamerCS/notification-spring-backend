package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.Notification;
import com.nurtore.notification_spring.service.TelegramNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TelegramNotificationSenderImpl implements TelegramNotificationSender {
    @Override
    public boolean send(Notification notification) {
        // TODO: Implement actual Telegram sending logic
        log.info("Sending Telegram notification to user: {}", notification.getUser().getId());
        return true;
    }
} 