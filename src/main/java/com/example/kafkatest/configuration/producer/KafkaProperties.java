package com.example.kafkatest.configuration.producer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(KafkaProperties.KafkaProducersProperties.class)
public class KafkaProperties {
    @RequiredArgsConstructor
    @ConfigurationProperties(prefix = "spring.kafka.producer")
    public static class KafkaProducersProperties {
        public final String bootstrapServers;
        public final Class<StringSerializer> keySerializer;
        public final Class<StringSerializer> valueSerializer;
        public final String acks;
        public final String compressionType;
    }
}
