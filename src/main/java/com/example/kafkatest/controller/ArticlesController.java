package com.example.kafkatest.controller;

import com.example.kafkatest.configuration.TrendingArticles;
import com.example.kafkatest.dto.request.CreateArticleRequest;
import com.example.kafkatest.service.ArticlesService;
import com.example.kafkatest.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticlesController {
    private final ArticlesService articlesService;
    private final RedisService redisService;

    @PostMapping("/makeArticle")
    public long makeArticle(@RequestBody CreateArticleRequest request) {
        long id = articlesService.createArticle(request);
        return id;
    }

    @GetMapping("/likeArticle")
    public void likeArticle(@RequestParam("articleId") long articleId) {
        articlesService.likeArticle(articleId);
    }

    @GetMapping("/getTrendingArticles")
    public List<TrendingArticles> getTrendingArticles() {
        List<avro.articles.TrendingArticles> trendingArticles = redisService.getTrendingArticles("trending.articles");
        List<TrendingArticles> ret = new ArrayList<>();
        trendingArticles.forEach((value) -> {
            TrendingArticles v = TrendingArticles.builder().articleId(value.getArticleId()).likes(value.getLikes()).build();
            ret.add(v);
        });

        return ret;
    }
}
