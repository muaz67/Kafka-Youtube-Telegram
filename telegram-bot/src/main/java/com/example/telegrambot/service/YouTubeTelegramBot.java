package com.example.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class YouTubeTelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeTelegramBot.class);

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.chat.id}")
    private String chatId;

    public YouTubeTelegramBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Handle incoming messages if needed
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            logger.info("Received message: {}", messageText);
            
            // You can implement command handling here if needed
            if (messageText.equals("/start")) {
                sendMessage("Welcome to YouTube Analytics Bot! You will receive notifications about videos with odd-length comments.");
            }
        }
    }

    public void sendMessage(String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(message);
            execute(sendMessage);
            logger.info("Message sent successfully: {}", message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to Telegram: ", e);
        }
    }

    public void sendVideoUpdate(String channelName, String videoUrl, Long commentCount) {
        String message = String.format("""
            🎥 New Video Update!
            Channel: %s
            Comments: %d
            URL: %s
            """, channelName, commentCount, videoUrl);
        
        sendMessage(message);
    }
} 