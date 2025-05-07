package com.nurtore.notification_spring.service;

import com.nurtore.notification_spring.model.User;

public interface EmailVerificationService {
    void sendVerificationEmail(User user);
    boolean verifyEmail(String token);
    void resendVerificationEmail(String email);
} 