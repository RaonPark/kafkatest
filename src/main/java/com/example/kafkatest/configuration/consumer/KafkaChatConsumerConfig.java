package com.example.kafkatest.configuration.consumer;

import com.example.kafkatest.configuration.properties.KafkaProperties;
import com.example.kafkatest.dto.ChatMessageKafkaDTO;
import com.example.kafkatest.entity.ChatMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
public class KafkaChatConsumerConfig {
    @Bean
    public ConsumerFactory<String, ChatMessage> chatConsumerFactory(KafkaProperties.KafkaConsumersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "chat");
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, properties.keyDeserializer);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configMap.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(configMap, new StringDeserializer(), new JsonDeserializer<>(ChatMessage.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> kafkaListenerContainerFactoryForChat(
            KafkaProperties.KafkaConsumersProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessage> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(chatConsumerFactory(properties));
        return containerFactory;
    }

    @Bean
    public ConsumerFactory<String, ChatMessageKafkaDTO> genericKafkaConsumerFactory(KafkaProperties.KafkaConsumersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, properties.groupIds.get(1));
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, properties.keyDeserializer);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configMap.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(configMap, new StringDeserializer(), new JsonDeserializer<>(ChatMessageKafkaDTO.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageKafkaDTO> kafkaListenerContainerFactoryForKVSW(KafkaProperties.KafkaConsumersProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageKafkaDTO> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(genericKafkaConsumerFactory(properties));
        return containerFactory;
    }
}
