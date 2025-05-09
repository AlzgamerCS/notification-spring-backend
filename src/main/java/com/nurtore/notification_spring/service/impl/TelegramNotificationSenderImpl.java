package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.Notification;
import com.nurtore.notification_spring.service.TelegramNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import jakarta.annotation.PostConstruct;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Service
public class TelegramNotificationSenderImpl extends TelegramLongPollingBot implements TelegramNotificationSender {
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.chat.id}")
    private String defaultChatId;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            log.info("Telegram bot successfully initialized");
        } catch (TelegramApiException e) {
            log.error("Failed to initialize Telegram bot", e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String firstName = update.getMessage().getFrom().getFirstName();
            
            try {
                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText(String.format("""
                    👋 Hello %s!
                    
                    Your Telegram Chat ID is: `%s`
                    
                    To receive notifications through this bot:
                    1. Copy this Chat ID
                    2. Add it to your profile during registration or update your profile
                    
                    Once set up, you'll receive document notifications here!
                    """, firstName, chatId));
                response.enableMarkdown(true);
                execute(response);
                log.info("Sent chat ID information to user: {}", chatId);
            } catch (TelegramApiException e) {
                log.error("Failed to send chat ID information", e);
            }
        }
    }

    @Override
    public boolean send(Notification notification) {
        String recipientChatId = notification.getUser().getTelegramChatId();
        if (recipientChatId == null || recipientChatId.isEmpty()) {
            log.info("Skipping Telegram notification for user {} as they have no Telegram chat ID configured", 
                    notification.getUser().getId());
            return false;
        }

        try {
            SendMessage message = new SendMessage();
            message.setChatId(recipientChatId);
            
            String text = String.format("""
                📄 Document Notification
                
                Document: %s
                Type: %s
                Status: %s
                
                This is an automated message.
                """,
                notification.getDocument().getTitle(),
                notification.getType(),
                notification.getStatus()
            );
            
            message.setText(text);
            message.enableMarkdown(true);
            
            execute(message);
            log.info("Successfully sent Telegram notification to chat: {}", recipientChatId);
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification", e);
            return false;
        }
    }
} 