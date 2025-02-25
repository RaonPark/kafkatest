package com.example.kafkatest.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TrendingArticles implements Serializable {
    private Long articleId;
    private long likes;
}
