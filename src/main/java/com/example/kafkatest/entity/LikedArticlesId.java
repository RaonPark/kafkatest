package com.example.kafkatest.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
public class LikedArticlesId implements Serializable {
    private Long articlesId;
    private UUID memberId;

    @Builder
    protected LikedArticlesId(Long articlesId, UUID memberId) {
        this.articlesId = articlesId;
        this.memberId = memberId;
    }
}
