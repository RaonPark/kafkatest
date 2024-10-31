package com.example.kafkatest.configuration;

import com.example.kafkatest.dto.request.PutMoneyRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public ProducerFactory<String, PutMoneyRequest> producerFactorForPutMoney(KafkaProducersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, properties.keySerializer);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configMap.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        // 조금 더 빠른 처리량을 위해 설정합니다.
        configMap.put(ProducerConfig.ACKS_CONFIG, properties.acks);
        configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));
        configMap.put(ProducerConfig.LINGER_MS_CONFIG, "20");
        // json의 경우는 snappy가 좋은 압축방법입니다.
        configMap.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(configMap, new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, PutMoneyRequest> kafkaTemplateForPutMoney(KafkaProducersProperties properties) {
        return new KafkaTemplate<>(producerFactorForPutMoney(properties));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateForTestMessage(KafkaProducersProperties properties) {
        return new KafkaTemplate<>(producerFactoryForStringValue(properties));
    }
}
