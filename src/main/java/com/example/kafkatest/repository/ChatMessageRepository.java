package com.example.kafkatest.repository;

import com.example.kafkatest.entity.ChatMessage;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("Select m from ChatMessage m where m.chatRoom.id = :chatroomId")
    List<ChatMessage> findChatMessageByChatroomId(@Param("chatroom_id") Long chatroomId);
}
