package com.example.kafkatest.repository;

import com.example.kafkatest.entity.LikedArticles;
import com.example.kafkatest.entity.LikedArticlesId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedArticlesRepository extends JpaRepository<LikedArticles, LikedArticlesId> {
}
