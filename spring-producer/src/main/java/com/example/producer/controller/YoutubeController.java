package com.example.producer.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.producer.service.YouTubeService;
import com.example.producer.model.YoutubeVideo;
import com.example.producer.repository.YoutubeVideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/youtube")
@CrossOrigin(origins = "*")
public class YoutubeController {

    private static final Logger logger = LoggerFactory.getLogger(YoutubeController.class);
    private final YouTubeService youtubeService;
    private final YoutubeVideoRepository videoRepository;

    public YoutubeController(YouTubeService youtubeService, YoutubeVideoRepository videoRepository) {
        this.youtubeService = youtubeService;
        this.videoRepository = videoRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchVideo(@RequestParam String query) {
        try {
            YoutubeVideo video;
            if (query.contains("youtube.com/") || query.contains("youtu.be/")) {
                // If the query is a full URL, use it directly
                video = youtubeService.fetchVideoData(query);
            } else {
                // If the query is just a video ID, construct the URL
                String videoUrl = "https://www.youtube.com/watch?v=" + query;
                video = youtubeService.fetchVideoData(videoUrl);
            }
            return ResponseEntity.ok(video);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid YouTube URL or video ID: {}", query, e);
            return ResponseEntity.badRequest().body("Invalid YouTube URL or video ID: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing video request: {}", query, e);
            return ResponseEntity.internalServerError().body("Error processing video request: " + e.getMessage());
        }
    }

    @GetMapping("/videos")
    public ResponseEntity<List<YoutubeVideo>> getAllVideos() {
        return ResponseEntity.ok(videoRepository.findAll());
    }

    @DeleteMapping("/videos/{videoId}")
    public ResponseEntity<?> deleteVideo(@PathVariable String videoId) {
        try {
            if (!videoRepository.existsById(videoId)) {
                return ResponseEntity.notFound().build();
            }
            videoRepository.deleteById(videoId);
            logger.info("Successfully deleted video with ID: {}", videoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting video with ID: {}", videoId, e);
            return ResponseEntity.internalServerError().body("Error deleting video: " + e.getMessage());
        }
    }
}