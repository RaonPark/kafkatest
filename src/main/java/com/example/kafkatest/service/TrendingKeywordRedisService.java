package com.example.kafkatest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.example.kafkatest.support.Constants.TRENDING_KEYWORD;

@Service
@RequiredArgsConstructor
public class TrendingKeywordRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    // sorted set의 멤버의 score를 증가시키는 redis 함수
    // redis의 명령어로는 zincrby key increment member 로 이뤄져있다.
    public void zincrby(String keyword, double increment) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(TRENDING_KEYWORD, keyword, increment);
    }

    // sorted set의 멤버를 score의 내림차순으로 가져오는 함수
    // zrevrange key start stop 으로 이뤄져있다.
    public Set<ZSetOperations.TypedTuple<Object>> zrevrange(int topN) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.reverseRangeWithScores(TRENDING_KEYWORD, 0, topN - 1);
    }
}
