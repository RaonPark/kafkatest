package com.example.kafkatest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditArticleRequest implements Serializable {
    private Long articleId;
    private String title;
    private String content;
    private Long liked;
    private String keyword;
}
