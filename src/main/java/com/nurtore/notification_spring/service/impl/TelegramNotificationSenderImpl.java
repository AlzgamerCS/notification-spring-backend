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
        // We don't need to handle incoming messages for now
        // This method is required by TelegramLongPollingBot
    }

    @Override
    public boolean send(Notification notification) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(defaultChatId);
            
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
            log.info("Successfully sent Telegram notification to chat: {}", defaultChatId);
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification", e);
            return false;
        }
    }
} 