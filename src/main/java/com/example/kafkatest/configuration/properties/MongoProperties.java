package com.example.kafkatest.configuration.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(MongoProperties.MongoDBProperties.class)
public class MongoProperties {
    @RequiredArgsConstructor
    @ConfigurationProperties(prefix = "spring.data.mongodb")
    public static class MongoDBProperties {
        public final String host;
        public final String port;
        public final String database;
    }
}
