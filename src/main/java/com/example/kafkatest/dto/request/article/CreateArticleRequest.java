package com.example.kafkatest.dto.request.article;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleRequest implements Serializable {
    private String title;
    private String content;
    private String keyword;
}
