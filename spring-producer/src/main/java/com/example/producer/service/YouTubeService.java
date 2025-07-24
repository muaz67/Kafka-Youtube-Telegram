package com.example.producer.service;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.producer.model.YoutubeVideo;
import com.example.producer.repository.YoutubeVideoRepository;
import com.example.producer.service.VideoCacheService.CacheEntry;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;

@Service
public class YouTubeService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*");

    private final YouTube youtubeService;
    private final YoutubeVideoRepository videoRepository;
    private final VideoCacheService cacheService;
    private final KafkaProducerService kafkaProducerService;
    
    @Value("${youtube.api.key}")
    private String apiKey;

    public YouTubeService(YouTube youtubeService, 
                         YoutubeVideoRepository videoRepository,
                         VideoCacheService cacheService,
                         KafkaProducerService kafkaProducerService) {
        this.youtubeService = youtubeService;
        this.videoRepository = videoRepository;
        this.cacheService = cacheService;
        this.kafkaProducerService = kafkaProducerService;
    }

    public String extractVideoId(String videoUrl) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(videoUrl);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new IllegalArgumentException("Invalid YouTube URL: " + videoUrl);
    }

    private Long convertBigIntegerToLong(BigInteger value) {
        return value != null ? value.longValue() : 0L;
    }

    public YoutubeVideo fetchVideoData(String videoUrl) {
        try {
            String videoId = extractVideoId(videoUrl);
            
            // Check cache first
            VideoCacheService.CacheEntry<YoutubeVideo> cachedVideo = cacheService.getCachedVideo(videoId);
            String videoEtag = cachedVideo != null ? cachedVideo.getEtag() : null;

            // Fetch video details with ETag
            YouTube.Videos.List videoRequest = youtubeService.videos()
                    .list(Collections.singletonList("statistics,snippet"))
                    .setKey(apiKey)
                    .setId(Collections.singletonList(videoId));

            if (videoEtag != null) {
                videoRequest.setRequestHeaders(new com.google.api.client.http.HttpHeaders().setIfNoneMatch(videoEtag));
            }

            VideoListResponse videoResponse = null;
            Video video;
            boolean videoChanged = true;

            try {
                videoResponse = videoRequest.execute();
                if (videoResponse.getItems().isEmpty()) {
                    throw new IllegalArgumentException("Video not found: " + videoId);
                }
                video = videoResponse.getItems().get(0);
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 304 && cachedVideo != null) {
                    // Not modified, use cached data
                    return cachedVideo.getData();
                } else {
                    throw e;
                }
            }

            String channelId = video.getSnippet().getChannelId();
            Channel channel = fetchChannelData(channelId);
            String commentText = "";

            try {
                YouTube.CommentThreads.List commentRequest = youtubeService.commentThreads()
                        .list(Collections.singletonList("snippet"))
                        .setKey(apiKey)
                        .setVideoId(videoId)
                        .setMaxResults(1L)
                        .setOrder("time");

                CommentThreadListResponse commentResponse = commentRequest.execute();
                if (!commentResponse.getItems().isEmpty()) {
                    CommentThread commentThread = commentResponse.getItems().get(0);
                    commentText = commentThread.getSnippet().getTopLevelComment().getSnippet().getTextDisplay();
                }
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 403) {
                    logger.info("Comments are disabled for video: {}", videoId);
                    // Set comment count to 0 for videos with disabled comments
                    video.getStatistics().setCommentCount(BigInteger.ZERO);
                } else {
                    throw e;
                }
            }

            // Create new video object
            YoutubeVideo youtubeVideo = new YoutubeVideo();
            youtubeVideo.setVideoId(videoId);
            youtubeVideo.setChannelName(video.getSnippet().getChannelTitle());
            youtubeVideo.setTitle(video.getSnippet().getTitle());
            youtubeVideo.setChannelId(channelId);
            youtubeVideo.setCommentCount(convertBigIntegerToLong(video.getStatistics().getCommentCount()));
            youtubeVideo.setLikeCount(convertBigIntegerToLong(video.getStatistics().getLikeCount()));
            youtubeVideo.setViewCount(convertBigIntegerToLong(video.getStatistics().getViewCount()));
            youtubeVideo.setPublishedAt(Instant.parse(video.getSnippet().getPublishedAt().toString()));
            youtubeVideo.setVideoUrl(videoUrl);
            youtubeVideo.setFetchedAt(Instant.now());
            youtubeVideo.setCommentText(commentText);
            
            // Set channel statistics
            youtubeVideo.setSubscriberCount(convertBigIntegerToLong(channel.getStatistics().getSubscriberCount()));
            youtubeVideo.setVideoCount(convertBigIntegerToLong(channel.getStatistics().getVideoCount()));

            // Cache video data
            if (videoResponse != null) {
                cacheService.cacheVideo(videoId, youtubeVideo, videoResponse.getEtag());
            }

            // Save to database
            videoRepository.save(youtubeVideo);

            // Send to Kafka immediately
            logger.info("Sending video data to Kafka for immediate processing: {}", videoId);
            kafkaProducerService.sendVideoData(youtubeVideo);

            return youtubeVideo;

        } catch (IOException e) {
            logger.error("Error fetching YouTube data: ", e);
            throw new RuntimeException("Failed to fetch YouTube data", e);
        }
    }

    private Channel fetchChannelData(String channelId) throws IOException {
        // Check channel cache
        VideoCacheService.CacheEntry<Channel> cachedChannel = cacheService.getCachedChannel(channelId);
        String channelEtag = cachedChannel != null ? cachedChannel.getEtag() : null;

        // Fetch channel details with ETag
        YouTube.Channels.List channelRequest = youtubeService.channels()
                .list(Collections.singletonList("statistics"))
                .setKey(apiKey)
                .setId(Collections.singletonList(channelId));

        if (channelEtag != null) {
            channelRequest.setRequestHeaders(new com.google.api.client.http.HttpHeaders().setIfNoneMatch(channelEtag));
        }

        try {
            ChannelListResponse channelResponse = channelRequest.execute();
            if (channelResponse.getItems().isEmpty()) {
                throw new IllegalArgumentException("Channel not found: " + channelId);
            }
            Channel channel = channelResponse.getItems().get(0);
            
            // Cache channel data
            cacheService.cacheChannelData(channelId, channel, channelResponse.getEtag());
            
            return channel;
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 304 && cachedChannel != null) {
                // Not modified, use cached data
                return cachedChannel.getData();
            } else {
                throw e;
            }
        }
    }
} 