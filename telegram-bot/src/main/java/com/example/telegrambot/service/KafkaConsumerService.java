package com.example.telegrambot.service;

import com.example.telegrambot.model.YoutubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final YouTubeTelegramBot telegramBot;

    public KafkaConsumerService(YouTubeTelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @KafkaListener(topics = "${kafka.topic.youtube-comments}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(YoutubeVideo video) {
        try {
            logger.info("Received video data from Kafka for video ID: {} ({})", video.getVideoId(), video.getTitle());
            
            // Since this topic is specifically for Telegram messages, we don't need to check comment length
            if (video.getCommentText() != null && !video.getCommentText().trim().isEmpty()) {
                logger.info("Processing video with comment for Telegram notification: {}", video.getTitle());
                
                String message = String.format("""
                    🎥 New YouTube Video Update!
                    
                    Title: %s
                    Channel: %s
                    Comment: %s
                    
                    Stats:
                    👁️ Views: %d
                    👍 Likes: %d
                    💬 Comments: %d
                    
                    URL: %s
                    """,
                    video.getTitle(),
                    video.getChannelName(),
                    video.getCommentText(),
                    video.getViewCount(),
                    video.getLikeCount(),
                    video.getCommentCount(),
                    video.getVideoUrl()
                );
                
                telegramBot.sendMessage(message);
                logger.info("Successfully sent Telegram notification for video: {}", video.getVideoId());
            } else {
                logger.warn("Received video without comment text in Telegram topic: {}", video.getVideoId());
            }
        } catch (Exception e) {
            logger.error("Error processing video data for Telegram: ", e);
            logger.debug("Full error details: ", e);
        }
    }
} 