package com.example.kafkatest.repository;

import com.example.kafkatest.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticlesRepository extends JpaRepository<Articles, Long> {
}
