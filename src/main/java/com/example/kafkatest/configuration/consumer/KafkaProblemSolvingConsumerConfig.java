package com.example.kafkatest.configuration.consumer;

import com.example.kafkatest.configuration.properties.KafkaProperties;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProblemSolvingConsumerConfig {
    @Bean
    public ConsumerFactory<String, Integer> genericAvroRecordKafkaConsumerFactoryForSolvedProblem(KafkaProperties.KafkaConsumersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "problem.solving.group");
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configMap.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, IntegerDeserializer.class);
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Integer> kafkaListenerContainerFactoryForProblemSolving(KafkaProperties.KafkaConsumersProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, Integer> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(genericAvroRecordKafkaConsumerFactoryForSolvedProblem(properties));

        return containerFactory;
    }
}
