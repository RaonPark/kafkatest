package com.example.kafkatest.configuration;

import avro.articles.TrendingArticles;
import com.example.ProblemSolving;
import com.example.kafkatest.dto.request.PutMoneyRequest;
import com.example.kafkatest.entity.ChatMessage;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaProducersConfig.KafkaProducersProperties.class)
public class KafkaProducersConfig {
    @RequiredArgsConstructor
    @ConfigurationProperties(prefix = "spring.kafka.producer")
    public static class KafkaProducersProperties {
        private final String bootstrapServers;
        private final Class<StringSerializer> keySerializer;
        private final Class<StringSerializer> valueSerializer;
        private final String acks;
        private final String compressionType;
    }

    @Bean
    public ProducerFactory<String, String> producerFactoryForStringValue(KafkaProducersProperties kafkaProducersProperties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducersProperties.bootstrapServers);
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducersProperties.keySerializer);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducersProperties.valueSerializer);
        configMap.put(ProducerConfig.ACKS_CONFIG, kafkaProducersProperties.acks);
        configMap.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProducersProperties.compressionType);

        return new DefaultKafkaProducerFactory<>(configMap);
    }

    @Bean
    public <T> ProducerFactory<String, T> jsonProducerFactory(KafkaProducersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, properties.keySerializer);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configMap.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        // 조금 더 빠른 처리량을 위해 설정합니다.
        configMap.put(ProducerConfig.ACKS_CONFIG, properties.acks);
        configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));
        configMap.put(ProducerConfig.LINGER_MS_CONFIG, "10");
        // json의 경우는 snappy가 좋은 압축방법입니다.
        configMap.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(configMap, new StringSerializer(), new JsonSerializer<>());
    }

//    @Bean
//    public KafkaTemplate<String, PutMoneyRequest> jsonKafkaTemplate(KafkaProducersProperties properties) {
//        return new KafkaTemplate<>(jsonProducerFactory(properties));
//    }
//
//    @Bean
//    public KafkaTemplate<String, ChatMessage> chatKafkaTemplate(KafkaProducersProperties properties) {
//        return new KafkaTemplate<>(jsonProducerFactory(properties));
//    }

    @Bean
    public <T> KafkaTemplate<String, T> genericJsonKafkaTemplate(KafkaProducersProperties properties) {
        return new KafkaTemplate<>(jsonProducerFactory(properties));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateForTestMessage(KafkaProducersProperties properties) {
        return new KafkaTemplate<>(producerFactoryForStringValue(properties));
    }

    @Bean
    public ProducerFactory<Long, TrendingArticles> avroRecordProducerFactory(KafkaProducersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        // 조금 더 빠른 처리량을 위해 설정합니다.
        configMap.put(ProducerConfig.ACKS_CONFIG, properties.acks);
        configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));
        configMap.put(ProducerConfig.LINGER_MS_CONFIG, "10");
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        // json의 경우는 snappy가 좋은 압축방법입니다.
        configMap.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(configMap);
    }

    @Bean
    @Primary
    public KafkaTemplate<Long, TrendingArticles> avroRecordKafkaTemplate(KafkaProducersProperties properties) {
        return new KafkaTemplate<>(avroRecordProducerFactory(properties));
    }

    @Bean
    public ProducerFactory<String, ProblemSolving> problemSolvingProducerFactory(KafkaProducersProperties properties) {
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
    public KafkaTemplate<String, ProblemSolving> problemSolvingKafkaTemplate(KafkaProducersProperties properties) {
        return new KafkaTemplate<>(problemSolvingProducerFactory(properties));
    }
}
