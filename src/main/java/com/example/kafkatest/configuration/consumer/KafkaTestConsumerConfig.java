package com.example.kafkatest.configuration.consumer;

import com.example.kafkatest.configuration.properties.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTestConsumerConfig {
    @Bean
    public ConsumerFactory<String, String> consumerFactoryKafkatest(KafkaProperties.KafkaConsumersProperties consumerProperties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProperties.keyDeserializer);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProperties.valueDeserializer);
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProperties.bootstrapServers);
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProperties.autoOffsetReset);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "kafkatest");
        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryKafkatest(
            KafkaProperties.KafkaConsumersProperties consumerProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryKafkatest(consumerProperties));
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryAnotherId(KafkaProperties.KafkaConsumersProperties consumerProperties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProperties.keyDeserializer);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProperties.valueDeserializer);
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProperties.bootstrapServers);
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProperties.autoOffsetReset);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "anotherId");
        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryAnotherId(
            KafkaProperties.KafkaConsumersProperties consumerProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryAnotherId(consumerProperties));
        return listenerContainerFactory;
    }
}
