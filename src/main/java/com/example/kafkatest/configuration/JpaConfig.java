package com.example.kafkatest.configuration;

import com.example.kafkatest.service.RedisService;
import com.example.kafkatest.support.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {
    @Bean
    public AuditorAware<UUID> auditorAware(RedisService redisService) {
        return new AuditorAwareImpl(redisService);
    }
}
