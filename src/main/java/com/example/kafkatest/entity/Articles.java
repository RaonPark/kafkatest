package com.example.kafkatest.entity;

import com.example.kafkatest.dto.request.CreateArticleRequest;
import com.example.kafkatest.dto.request.EditArticleRequest;
import com.example.kafkatest.support.BaseUserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@SequenceGenerator(name = "ARTICLES_SEQ", allocationSize = 100, initialValue = 1)
public class Articles extends BaseUserEntity {
    @Id
    @GeneratedValue(generator = "ARTICLE_SEQ", strategy = GenerationType.SEQUENCE)
    @Column(name = "articles_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    @Setter
    private Long liked;

    protected Articles() { }

    @Builder(
            builderMethodName = "buildWithCreateArticleRequest"
    )
    protected Articles(CreateArticleRequest createArticleRequest) {
        this.title = createArticleRequest.getTitle();
        this.content = createArticleRequest.getContent();
        this.keyword = createArticleRequest.getKeyword();
        this.liked = 0L;
    }

    @Builder(
            builderMethodName = "buildWithEditArticleRequest"
    )
    protected Articles(EditArticleRequest editArticleRequest) {
        this.id = editArticleRequest.getArticleId();
        this.content = editArticleRequest.getContent();
        this.title = editArticleRequest.getTitle();
        this.liked = editArticleRequest.getLiked();
        this.keyword = editArticleRequest.getKeyword();
    }
}
