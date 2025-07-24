package com.example.producer.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import com.example.producer.model.YoutubeVideo;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;

@Service
public class VideoCacheService {
    private final Map<String, CacheEntry<YoutubeVideo>> videoCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<Channel>> channelCache = new ConcurrentHashMap<>();

    public static class CacheEntry<T> {
        private final T data;
        private final String etag;
        private final Instant timestamp;

        public CacheEntry(T data, String etag) {
            this.data = data;
            this.etag = etag;
            this.timestamp = Instant.now();
        }

        public T getData() {
            return data;
        }

        public String getEtag() {
            return etag;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public void cacheVideo(String videoId, YoutubeVideo video, String etag) {
        videoCache.put(videoId, new CacheEntry<>(video, etag));
    }

    public CacheEntry<YoutubeVideo> getCachedVideo(String videoId) {
        return videoCache.get(videoId);
    }

    public void cacheChannelData(String channelId, Channel channel, String etag) {
        channelCache.put(channelId, new CacheEntry<>(channel, etag));
    }

    public CacheEntry<Channel> getCachedChannel(String channelId) {
        return channelCache.get(channelId);
    }

    public void clearCache() {
        videoCache.clear();
        channelCache.clear();
    }
} 