package com.example.producer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.producer.model.YoutubeVideoAnalytics;

@Repository
public interface YoutubeVideoAnalyticsRepository extends JpaRepository<YoutubeVideoAnalytics, Long> {
    // Add custom queries if needed
} 