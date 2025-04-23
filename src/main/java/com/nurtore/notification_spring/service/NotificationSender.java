package com.nurtore.notification_spring.service;

import com.nurtore.notification_spring.model.Notification;

public interface NotificationSender {
    boolean send(Notification notification);
} 