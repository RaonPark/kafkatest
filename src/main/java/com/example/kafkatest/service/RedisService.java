package com.example.kafkatest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("ObjectMapper 에러!");
        }
    }

    // 이 함수는 이제 제네릭으로 쓰일 것을 암시한다.
    public <T> T find(String key, Class<T> valueType) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if(jsonValue == null) {
                return null;
            }
            return objectMapper.readValue(jsonValue, valueType);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Find에서 ObjectMapper오류!");
        }
    }

    public <HK, V> void saveHash(String key, HK hashKey, V value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForHash().put(key, hashKey, jsonValue);
        } catch(JsonProcessingException ex) {
            throw new RuntimeException("ObjectMapper 에러!");
        }
    }

    public <HK, V> V findHash(String key, HK hashKey, Class<V> valueType) {
        try {
            String jsonValue = (String) redisTemplate.opsForHash().get(key, hashKey);
            if(jsonValue == null) {
                return null;
            }
            return objectMapper.readValue(jsonValue, valueType);
        } catch(JsonProcessingException ex) {
            throw new RuntimeException("ObjectMapper 에러!");
        }
    }
}
