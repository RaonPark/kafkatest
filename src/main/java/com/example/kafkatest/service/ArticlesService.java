package com.example.kafkatest.service;

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
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticlesService {
    private final ArticlesRepository articlesRepository;
    private final LikedArticlesRepository likedArticlesRepository;
    private final RedisService redisService;
    private final TrendingKeywordRedisService trendingKeywordRedisService;
    private final MemberRepository memberRepository;

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
}
