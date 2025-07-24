package com.example.producer.service;

import java.util.concurrent.CompletableFuture;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.producer.model.YoutubeVideo;
import com.example.producer.model.YoutubeVideoAnalytics;
import com.example.producer.repository.YoutubeVideoAnalyticsRepository;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, YoutubeVideo> kafkaTemplate;
    private final YoutubeVideoAnalyticsRepository analyticsRepository;
    
    @Value("${kafka.topic.youtube-data}")
    private String analyticsTopic;

    @Value("${kafka.topic.youtube-comments}")
    private String commentsTopic;

    @Value("${kafka.topic.youtube-delete}")
    private String deleteTopic;

    public KafkaProducerService(KafkaTemplate<String, YoutubeVideo> kafkaTemplate, 
                               YoutubeVideoAnalyticsRepository analyticsRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.analyticsRepository = analyticsRepository;
    }

    public void sendVideoData(YoutubeVideo video) {
        if (video == null) {
            logger.warn("Received null video object, skipping processing");
            return;
        }

        logger.info("Processing video: {} ({})", video.getTitle(), video.getVideoId());
        logger.debug("Topics configured - Analytics: {}, Comments: {}", analyticsTopic, commentsTopic);

        String commentText = video.getCommentText();
        logger.debug("Comment text for video {}: '{}'", video.getVideoId(), commentText);
        
        if (commentText != null && !commentText.trim().isEmpty()) {
            commentText = commentText.trim();
            int commentLength = commentText.length();
            
            logger.info("Video {} has comment with length {} - Comment text: '{}'", 
                video.getVideoId(), commentLength, commentText);

            if (commentLength % 2 == 0) {
                // Even length - send to consumer for analytics processing
                logger.info("Video {} has even comment length ({}), sending to analytics topic: {}", 
                video.getVideoId(), commentLength, analyticsTopic);
                sendToAnalytics(video);
            } else {
                // Odd length - send to Telegram
                logger.info("Video {} has odd comment length ({}), sending to Telegram topic: {}", 
                video.getVideoId(), commentLength, commentsTopic);
                try {
                    sendToTelegram(video);
                } catch (Exception e) {
                    logger.error("Failed to send to Telegram, will retry later: {}", e.getMessage());
                }
            }
        } else {
            logger.info("Video {} has no comments, skipping Kafka message routing", video.getVideoId());
        }
    }

    public void sendDeleteNotification(String videoId) {
        if (videoId == null || videoId.trim().isEmpty()) {
            logger.warn("Received null or empty videoId for deletion, skipping");
            return;
        }

        logger.info("Sending delete notification for video: {}", videoId);
        
        // Create a video object with just the ID for deletion
        YoutubeVideo deleteVideo = new YoutubeVideo();
        deleteVideo.setVideoId(videoId);
        
        CompletableFuture<SendResult<String, YoutubeVideo>> future = 
            kafkaTemplate.send(deleteTopic, videoId, deleteVideo);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent delete notification for video ID: {} with offset: {}", 
                    videoId, 
                    result.getRecordMetadata().offset());
            } else {
                logger.error("Unable to send delete notification for video ID: {} due to : {}", 
                    videoId, 
                    ex.getMessage());
            }
        });
    }

    private void sendToAnalytics(YoutubeVideo video) {
        CompletableFuture<SendResult<String, YoutubeVideo>> future = 
            kafkaTemplate.send(analyticsTopic, video.getVideoId(), video);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent video data for analytics, ID: {} with offset: {}", 
                    video.getVideoId(), 
                    result.getRecordMetadata().offset());
            } else {
                logger.error("Unable to send video data for analytics, ID: {} due to : {}", 
                    video.getVideoId(), 
                    ex.getMessage());
            }
        });
    }

    private void sendToTelegram(YoutubeVideo video) {
        try {
            // Send to Kafka for Telegram bot
            CompletableFuture<SendResult<String, YoutubeVideo>> future = 
                kafkaTemplate.send(commentsTopic, video.getVideoId(), video);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent video data to Telegram topic, ID: {} with offset: {}", 
                        video.getVideoId(), 
                        result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send video data to Telegram topic, ID: {} due to : {}", 
                        video.getVideoId(), 
                        ex.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Failed to send video to Telegram topic, ID: {}", video.getVideoId(), e);
        }
    }
} 