package com.example.kafkatest.repository.article;

import com.example.kafkatest.entity.chat.Articles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticlesRepository extends JpaRepository<Articles, Long> {
}
