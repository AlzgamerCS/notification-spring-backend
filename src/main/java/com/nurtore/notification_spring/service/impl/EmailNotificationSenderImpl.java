package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.Notification;
import com.nurtore.notification_spring.service.EmailNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationSenderImpl implements EmailNotificationSender {
    private final JavaMailSender emailSender;

    @Override
    public boolean send(Notification notification) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // helper.setTo("nurtore.kelessov@nu.edu.kz");
            helper.setTo(notification.getUser().getEmail());
            helper.setSubject("Document Notification: " + notification.getDocument().getTitle());
            
            String htmlContent = String.format("""
                <html>
                    <body>
                        <h2>Document Notification</h2>
                        <p>Hello %s,</p>
                        <p>You have a new notification regarding the document: <strong>%s</strong></p>
                        <p>Type: %s</p>
                        <p>Status: %s</p>
                        <hr>
                        <p>This is an automated message, please do not reply.</p>
                    </body>
                </html>
                """,
                notification.getUser().getName(),
                notification.getDocument().getTitle(),
                notification.getType(),
                notification.getStatus()
            );
            
            helper.setText(htmlContent, true);
            emailSender.send(message);
            
            log.info("Successfully sent email notification to user: {}", notification.getUser().getId());
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send email notification to user: {}", notification.getUser().getId(), e);
            return false;
        }
    }
} 