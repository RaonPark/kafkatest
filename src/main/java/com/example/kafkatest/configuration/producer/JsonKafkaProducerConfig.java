package com.example.kafkatest.configuration.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JsonKafkaProducerConfig {
    @Bean
    public ProducerFactory<String, String> producerFactoryForStringValue(KafkaProperties.KafkaProducersProperties kafkaProperties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers);
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProperties.keySerializer);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProperties.valueSerializer);
        configMap.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.acks);
        configMap.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProperties.compressionType);

        return new DefaultKafkaProducerFactory<>(configMap);
    }

    @Bean
    public <T> ProducerFactory<String, T> jsonProducerFactory(KafkaProperties.KafkaProducersProperties properties) {
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

    @Bean
    public <T> KafkaTemplate<String, T> jsonKafkaTemplate(KafkaProperties.KafkaProducersProperties properties) {
        return new KafkaTemplate<>(jsonProducerFactory(properties));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateForTestMessage(KafkaProperties.KafkaProducersProperties properties) {
        return new KafkaTemplate<>(producerFactoryForStringValue(properties));
    }
}
