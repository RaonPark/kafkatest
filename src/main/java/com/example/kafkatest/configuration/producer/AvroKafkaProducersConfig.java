package com.example.kafkatest.configuration.producer;

import avro.articles.TrendingArticles;
import com.example.Payments;
import com.example.ProblemSolving;
import com.example.kafkatest.configuration.properties.KafkaProperties;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AvroKafkaProducersConfig {
    @Bean
    public <T extends SpecificRecord> ProducerFactory<String, T> genericAvroKafkaProducer(KafkaProperties.KafkaProducersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        // 조금 더 빠른 처리량을 위해 설정합니다.
        configMap.put(ProducerConfig.ACKS_CONFIG, properties.acks);
        configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));
        configMap.put(ProducerConfig.LINGER_MS_CONFIG, "20");
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        // json의 경우는 snappy가 좋은 압축방법입니다.
        configMap.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(configMap);
    }

    @Bean
    public KafkaTemplate<String, ProblemSolving> problemSolvingKafkaTemplate(KafkaProperties.KafkaProducersProperties properties) {
        return new KafkaTemplate<>(genericAvroKafkaProducer(properties));
    }

    @Bean
    public KafkaTemplate<String, TrendingArticles> trendingArticlesKafkaTemplate(KafkaProperties.KafkaProducersProperties properties) {
        return new KafkaTemplate<>(genericAvroKafkaProducer(properties));
    }

    @Bean
    public KafkaTemplate<String, Payments> paymentsKafkaTemplate(KafkaProperties.KafkaProducersProperties properties) {
        return new KafkaTemplate<>(genericAvroKafkaProducer(properties));
    }
}
