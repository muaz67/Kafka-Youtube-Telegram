package com.example.producer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "youtube_videos_analytics")
public class YoutubeVideoAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String videoId;
    private String title;
    private String channelName;
    private Long commentCount;
    private Long likeCount;
    private Long subscriberCount;
    private Long videoCount;
    private Long viewCount;
    private String videoUrl;
    private Instant receivedAt;
    private Instant lastUpdated;
    private String commentText;

    // Default constructor
    public YoutubeVideoAnalytics() {}

    // Constructor from YoutubeVideo
    public YoutubeVideoAnalytics(YoutubeVideo video) {
        this.videoId = video.getVideoId();
        this.title = video.getTitle();
        this.channelName = video.getChannelName();
        this.commentCount = video.getCommentCount() != null ? video.getCommentCount() : 0L;
        this.commentText = video.getCommentText();
        this.likeCount = video.getLikeCount() != null ? video.getLikeCount() : 0L;
        this.viewCount = video.getViewCount() != null ? video.getViewCount() : 0L;
        this.videoUrl = video.getVideoUrl();
        this.subscriberCount = video.getSubscriberCount(); // Will be updated by YouTube API
        this.videoCount = video.getVideoCount(); // Will be updated by YouTube API
        this.receivedAt = Instant.now();
        this.lastUpdated = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }

    public Long getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(Long subscriberCount) { this.subscriberCount = subscriberCount; }

    public Long getVideoCount() { return videoCount; }
    public void setVideoCount(Long videoCount) { this.videoCount = videoCount; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
} 