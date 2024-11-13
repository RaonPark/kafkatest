package com.example.kafkatest.support;

import com.example.kafkatest.entity.Member;
import com.example.kafkatest.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditorAwareImpl implements AuditorAware<UUID> {
    private final RedisService redisService;

    @Override
    public Optional<UUID> getCurrentAuditor() {
        log.info("get current auditor: {}", redisService.find("currentUser", UUID.class));
        return Optional.ofNullable(redisService.find("currentUser", UUID.class));
    }
}
