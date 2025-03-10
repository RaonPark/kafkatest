package com.example.kafkatest.repository.chat;

import com.example.kafkatest.entity.chat.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
