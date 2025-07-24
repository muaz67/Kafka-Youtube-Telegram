package com.example.producer.repository;

import com.example.producer.model.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, String> {
    // Add custom queries if needed
} 