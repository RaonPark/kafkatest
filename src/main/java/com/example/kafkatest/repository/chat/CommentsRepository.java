package com.example.kafkatest.repository.chat;

import com.example.kafkatest.entity.article.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
}
