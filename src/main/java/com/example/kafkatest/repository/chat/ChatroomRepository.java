package com.example.kafkatest.repository.chat;

import com.example.kafkatest.entity.chat.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
}
