package com.example.consumer.service;

import com.example.consumer.model.YoutubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    
    // In-memory storage for video data
    private final Map<String, YoutubeVideo> videoCache = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.topic.youtube-data}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(YoutubeVideo video) {
        try {
            if (video == null || video.getVideoId() == null) {
                logger.warn("Received null video or video ID, skipping processing");
                return;
            }

            logger.info("Processing video update for ID: {}", video.getVideoId());
            
            // Update video in cache
            videoCache.put(video.getVideoId(), video);
            
            logger.info("Successfully processed video data. ID: {}, Title: {}", 
                video.getVideoId(), video.getTitle());
            
        } catch (Exception e) {
            logger.error("Error processing video update: ", e);
            throw e; // Rethrow to trigger Kafka retry
        }
    }

    @KafkaListener(topics = "${kafka.topic.youtube-delete}")
    public void handleDeleteNotification(YoutubeVideo video) {
        if (video == null || video.getVideoId() == null) {
            logger.warn("Received null video or video ID for deletion");
            return;
        }

        String videoId = video.getVideoId();
        logger.info("Received delete notification for video: {}", videoId);

        try {
            videoCache.remove(videoId);
            logger.info("Successfully deleted video from cache: {}", videoId);
        } catch (Exception e) {
            logger.error("Error deleting video {}: {}", videoId, e.getMessage());
        }
    }

    // Analytics methods
    public List<YoutubeVideo> getAllVideos() {
        return new ArrayList<>(videoCache.values());
    }

    public Map<String, Object> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        List<YoutubeVideo> videos = getAllVideos();

        if (videos.isEmpty()) {
            return Collections.emptyMap();
        }

        // Calculate total statistics
        long totalViews = 0;
        long totalLikes = 0;
        long totalComments = 0;
        long totalVideos = videos.size();

        // Find highest metrics
        YoutubeVideo highestViews = null;
        YoutubeVideo highestLikes = null;
        YoutubeVideo highestComments = null;
        YoutubeVideo highestSubscribers = null;
        YoutubeVideo highestVideoCount = null;
        YoutubeVideo highestEngagement = null;
        double maxEngagementRate = 0.0;

        for (YoutubeVideo video : videos) {
            // Update totals
            totalViews += video.getViewCount() != null ? video.getViewCount() : 0;
            totalLikes += video.getLikeCount() != null ? video.getLikeCount() : 0;
            totalComments += video.getCommentCount() != null ? video.getCommentCount() : 0;

            // Calculate engagement rate for this video
            long views = video.getViewCount() != null ? video.getViewCount() : 0;
            long likes = video.getLikeCount() != null ? video.getLikeCount() : 0;
            long comments = video.getCommentCount() != null ? video.getCommentCount() : 0;
            double engagementRate = views > 0 ? ((likes + comments) * 100.0) / views : 0;

            // Update highest metrics
            if (highestViews == null || (video.getViewCount() != null && video.getViewCount() > highestViews.getViewCount())) {
                highestViews = video;
            }
            if (highestLikes == null || (video.getLikeCount() != null && video.getLikeCount() > highestLikes.getLikeCount())) {
                highestLikes = video;
            }
            if (highestComments == null || (video.getCommentCount() != null && video.getCommentCount() > highestComments.getCommentCount())) {
                highestComments = video;
            }
            if (highestSubscribers == null || (video.getSubscriberCount() != null && video.getSubscriberCount() > highestSubscribers.getSubscriberCount())) {
                highestSubscribers = video;
            }
            if (highestVideoCount == null || (video.getVideoCount() != null && video.getVideoCount() > highestVideoCount.getVideoCount())) {
                highestVideoCount = video;
            }
            if (engagementRate > maxEngagementRate) {
                maxEngagementRate = engagementRate;
                highestEngagement = video;
            }
        }

        // Calculate average engagement rate
        double averageEngagementRate = totalViews > 0 ? ((totalLikes + totalComments) * 100.0) / totalViews : 0;

        // Add totals to analytics
        analytics.put("totalVideos", totalVideos);
        analytics.put("totalViews", totalViews);
        analytics.put("totalLikes", totalLikes);
        analytics.put("totalComments", totalComments);
        analytics.put("averageEngagementRate", String.format("%.2f%%", averageEngagementRate));

        // Add highest metrics to analytics
        Map<String, YoutubeVideo> highest = new HashMap<>();
        highest.put("views", highestViews);
        highest.put("likes", highestLikes);
        highest.put("comments", highestComments);
        highest.put("subscribers", highestSubscribers);
        highest.put("videos", highestVideoCount);
        highest.put("engagement", highestEngagement);
        analytics.put("highest", highest);

        return analytics;
    }
} 