package com.example.producer.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "youtube_videos")
public class YoutubeVideo {
    
    @Id
    private String videoId;
    private String title;
    private String channelId;
    private String channelName;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private String commentText;
    private Instant publishedAt;
    private String videoUrl;
    private Instant fetchedAt;
    private Long subscriberCount;
    private Long videoCount;

    // Default constructor
    public YoutubeVideo() {}

    // Getters and Setters
    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }

    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }

    public Long getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(Long subscriberCount) { this.subscriberCount = subscriberCount; }

    public Long getVideoCount() { return videoCount; }
    public void setVideoCount(Long videoCount) { this.videoCount = videoCount; }

    @Override
    public String toString() {
        return "YoutubeVideo{" +
                "videoId='" + videoId + '\'' +
                ", title='" + title + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", commentText='" + commentText + '\'' +
                ", publishedAt=" + publishedAt +
                ", videoUrl='" + videoUrl + '\'' +
                ", fetchedAt=" + fetchedAt +
                ", subscriberCount=" + subscriberCount +
                ", videoCount=" + videoCount +
                '}';
    }
} 