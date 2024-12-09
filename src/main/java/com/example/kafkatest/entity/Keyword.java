package com.example.kafkatest.entity;

import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "KEYWORD_SEQ", allocationSize = 100, initialValue = 1)
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "KEYWORD_SEQ")
    private Long id;

    @Column(nullable = false)
    private String keyword;
}
