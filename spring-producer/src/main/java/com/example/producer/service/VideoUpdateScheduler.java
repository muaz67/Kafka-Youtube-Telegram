package com.example.producer.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.producer.model.YoutubeVideo;
import com.example.producer.repository.YoutubeVideoRepository;

@Service
public class VideoUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VideoUpdateScheduler.class);

    private final YouTubeService youtubeService;
    private final KafkaProducerService kafkaProducerService;
    private final YoutubeVideoRepository videoRepository;

    @Value("${youtube.fetch.interval:300000}") // Default to 5 minutes if not specified
    private long fetchInterval;

    public VideoUpdateScheduler(YouTubeService youtubeService,
                              KafkaProducerService kafkaProducerService,
                              YoutubeVideoRepository videoRepository) {
        this.youtubeService = youtubeService;
        this.kafkaProducerService = kafkaProducerService;
        this.videoRepository = videoRepository;
    }

    @Scheduled(fixedDelayString = "${youtube.fetch.interval:300000}") // Use the same interval from properties
    public void updateVideoData() {
        logger.info("Starting scheduled video data update...");
        List<YoutubeVideo> videos = videoRepository.findAll();
        
        if (videos.isEmpty()) {
            logger.info("No videos found in database. Please add videos through the web interface.");
            return;
        }
        
        for (YoutubeVideo video : videos) {
            try {
                logger.info("Updating data for video ID: {} ({})", video.getVideoId(), video.getTitle());
                
                // Fetch fresh data with ETag support
                YoutubeVideo updatedVideo = youtubeService.fetchVideoData(video.getVideoUrl());
                
                // Only send to Kafka if there are changes in comments or statistics
                if (hasSignificantChanges(video, updatedVideo)) {
                    kafkaProducerService.sendVideoData(updatedVideo);
                    logger.info("Changes detected, sent updated data for video: {} ({})", 
                        video.getVideoId(), video.getTitle());
                } else {
                    logger.debug("No significant changes for video: {} ({})", 
                        video.getVideoId(), video.getTitle());
                }
                
            } catch (Exception e) {
                logger.error("Error updating video ID: {} - {}", video.getVideoId(), e.getMessage());
                // Continue with next video even if one fails
            }
        }
        logger.info("Completed scheduled video data update for {} videos", videos.size());
    }

    private boolean hasSignificantChanges(YoutubeVideo oldVideo, YoutubeVideo newVideo) {
        logger.debug("Checking for changes in video: {}", newVideo.getVideoId());
        logger.debug("Old comment: '{}', New comment: '{}'", 
            oldVideo.getCommentText(), newVideo.getCommentText());
        
        // Always process if either video has comments
        if (oldVideo.getCommentText() != null || newVideo.getCommentText() != null) {
            logger.info("Video {} has comments, will process", newVideo.getVideoId());
            return true;
        }

        // Check if statistics have changed
        boolean statsChanged = !oldVideo.getCommentCount().equals(newVideo.getCommentCount()) ||
            !oldVideo.getLikeCount().equals(newVideo.getLikeCount()) ||
            !oldVideo.getViewCount().equals(newVideo.getViewCount()) ||
            !oldVideo.getSubscriberCount().equals(newVideo.getSubscriberCount()) ||
            !oldVideo.getVideoCount().equals(newVideo.getVideoCount());
            
        if (statsChanged) {
            logger.info("Video {} stats have changed, will process", newVideo.getVideoId());
            logger.debug("Old stats - Views: {}, Likes: {}, Comments: {}, Subscribers: {}, Videos: {}", 
                oldVideo.getViewCount(), oldVideo.getLikeCount(), oldVideo.getCommentCount(),
                oldVideo.getSubscriberCount(), oldVideo.getVideoCount());
            logger.debug("New stats - Views: {}, Likes: {}, Comments: {}, Subscribers: {}, Videos: {}", 
                newVideo.getViewCount(), newVideo.getLikeCount(), newVideo.getCommentCount(),
                newVideo.getSubscriberCount(), newVideo.getVideoCount());
        }

        return statsChanged;
    }

    private boolean stringsEqual(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }
} 