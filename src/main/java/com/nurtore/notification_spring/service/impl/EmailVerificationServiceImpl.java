package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.EmailVerificationToken;
import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.repository.EmailVerificationTokenRepository;
import com.nurtore.notification_spring.repository.UserRepository;
import com.nurtore.notification_spring.service.EmailVerificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender emailSender;
    
    private static final int TOKEN_LENGTH = 6;
    private static final int TOKEN_EXPIRY_MINUTES = 15;

    @Override
    public void sendVerificationEmail(User user) {
        // Generate OTP token
        String token = generateOTP();
        
        // Save token
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        verificationToken.setUsed(false);
        tokenRepository.save(verificationToken);

        // Send email
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Email Verification");
            
            String htmlContent = String.format("""
                <html>
                    <body>
                        <h2>Email Verification</h2>
                        <p>Hello %s,</p>
                        <p>Your verification code is: <strong>%s</strong></p>
                        <p>This code will expire in %d minutes.</p>
                        <p>If you didn't request this verification, please ignore this email.</p>
                        <hr>
                        <p>This is an automated message, please do not reply.</p>
                    </body>
                </html>
                """,
                user.getName(),
                token,
                TOKEN_EXPIRY_MINUTES
            );
            
            helper.setText(htmlContent, true);
            emailSender.send(message);
            
            log.info("Verification email sent to user: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send verification email to user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new EntityNotFoundException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new IllegalStateException("Token has already been used");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token has expired");
        }

        // Mark token as used
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        // Update user's email verification status
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        return true;
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Invalidate any existing unused tokens
        tokenRepository.findByUserAndUsedFalse(user)
            .ifPresent(token -> {
                token.setUsed(true);
                tokenRepository.save(token);
            });

        // Send new verification email
        sendVerificationEmail(user);
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }
} 