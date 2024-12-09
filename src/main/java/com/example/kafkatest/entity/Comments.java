package com.example.kafkatest.entity;

import com.example.kafkatest.support.BaseUserEntity;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "COMMENTS_SEQ", allocationSize = 100, initialValue = 1)
public class Comments extends BaseUserEntity {
    @Id
    @GeneratedValue(generator = "COMMENTS_SEQ", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String content;
}
