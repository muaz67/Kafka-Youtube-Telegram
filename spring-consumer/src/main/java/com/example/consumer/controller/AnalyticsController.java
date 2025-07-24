package com.example.consumer.controller;

import com.example.consumer.model.YoutubeVideo;
import com.example.consumer.service.KafkaConsumerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, 
    RequestMethod.DELETE, RequestMethod.OPTIONS
})
public class AnalyticsController {

    private final KafkaConsumerService kafkaConsumerService;

    public AnalyticsController(KafkaConsumerService kafkaConsumerService) {
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @GetMapping("/highest")
    public ResponseEntity<Map<String, Object>> getHighestStats() {
        Map<String, Object> analytics = kafkaConsumerService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/videos")
    public ResponseEntity<List<YoutubeVideo>> getAllVideos() {
        return ResponseEntity.ok(kafkaConsumerService.getAllVideos());
    }

    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getVideoComparison() {
        Map<String, Object> analytics = kafkaConsumerService.getAnalytics();
        if (analytics.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No videos available for comparison"));
        }
        return ResponseEntity.ok(analytics);
    }
} 