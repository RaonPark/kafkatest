package com.example.kafkatest.service;

import avro.articles.TrendingArticles;
import com.example.kafkatest.dto.request.CreateArticleRequest;
import com.example.kafkatest.dto.request.EditArticleRequest;
import com.example.kafkatest.dto.request.LikeArticleRequest;
import com.example.kafkatest.entity.Articles;
import com.example.kafkatest.entity.LikedArticles;
import com.example.kafkatest.entity.Member;
import com.example.kafkatest.repository.ArticlesRepository;
import com.example.kafkatest.repository.LikedArticlesRepository;
import com.example.kafkatest.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ArticlesService {
    private final ArticlesRepository articlesRepository;
    private final LikedArticlesRepository likedArticlesRepository;
    private final RedisService redisService;
    private final TrendingKeywordRedisService trendingKeywordRedisService;
    private final MemberRepository memberRepository;
    private final KafkaTemplate<String, TrendingArticles> trendingArticlesKafkaTemplate;
    public Long createArticle(CreateArticleRequest request) {
        Articles article = Articles.buildWithCreateArticleRequest().createArticleRequest(request).build();

        // 키워드를 분석하여 넣어놓는다.

        return articlesRepository.save(article).getId();
    }

    public void editArticle(EditArticleRequest request) {
        Articles article = Articles.buildWithEditArticleRequest().editArticleRequest(request).build();

        articlesRepository.save(article);
    }

    public long likeArticle(LikeArticleRequest request) {
        // 유저 정보 찾기
        String userId = redisService.find("currentUser", String.class);
        Member member = memberRepository.findByUserId(userId);

        // 게시물 정보 찾기
        Articles article = articlesRepository.findById(request.getArticlesId())
                .orElseThrow(RuntimeException::new);
        article.setLiked(article.getLiked() + 1);

        LikedArticles likedArticles = LikedArticles.builder()
                .articles(article).member(member).build();
        likedArticlesRepository.save(likedArticles);

        return article.getLiked();
    }

    public void likeArticle(Long articleId) {
        trendingArticlesKafkaTemplate.send("articles.topic",
                articleId.toString(), new TrendingArticles(articleId, 1L));
    }

    @KafkaListener(topics = "trending.articles.topic", groupId = "trending.articles.group", containerFactory = "kafkaListenerContainerFactoryForTrendingArticles")
    public void getTrendingArticles(ConsumerRecord<Long, TrendingArticles> record) {
        TrendingArticles trendingArticles = record.value();
        log.info("trending articles : id = {} likes = {}", trendingArticles.getArticleId(), trendingArticles.getLikes());
        redisService.saveHash("trending.articles", String.valueOf(record.key()), String.valueOf(trendingArticles.getLikes()));
    }
}
