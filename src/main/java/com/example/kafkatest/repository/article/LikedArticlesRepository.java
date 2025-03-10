package com.example.kafkatest.repository.article;

import com.example.kafkatest.entity.chat.LikedArticles;
import com.example.kafkatest.entity.article.LikedArticlesId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedArticlesRepository extends JpaRepository<LikedArticles, LikedArticlesId> {
}
