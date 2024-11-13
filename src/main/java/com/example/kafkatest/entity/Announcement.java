package com.example.kafkatest.entity;

import com.example.kafkatest.support.BaseUserEntity;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "ANNOUNCEMENT_SEQ", allocationSize = 100)
public class Announcement extends BaseUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ANNOUNCEMENT_SEQ")
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatRoom;

    protected Announcement() {

    }
}
