package com.example.kafkatest.entity;

import com.example.kafkatest.support.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;

@Entity
public class LikedArticles extends BaseTimeEntity {
    @EmbeddedId
    private LikedArticlesId id;

    @ManyToOne
    @MapsId("articlesId")
    @JoinColumn(name = "articles_id")
    private Articles articles;

    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    protected LikedArticles() {

    }

    @Builder
    public LikedArticles(Articles articles, Member member) {
        this.articles = articles;
        this.member = member;
    }
}
