package com.example.kafkatest.configuration.consumer;

import com.example.kafkatest.configuration.properties.KafkaProperties;
import com.example.kafkatest.dto.request.PutMoneyRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaBalanceTestConsumerConfig {
    @Bean
    public ConsumerFactory<String, PutMoneyRequest> consumerFactoryForBalanceDisplay(KafkaProperties.KafkaConsumersProperties consumerProperties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // JsonDeserializer를 사용하기 위한 config 변수들
        // 헤더에 패키지 정보를 넣어두기 때문에 producer와 consumer간의 패키지 정보가 맞지 않으면 문제가 생길 수 있다.
        // 따라서 trusted-package와 use-type-info-headers 정보를 설정해주고
        // 한글이 들어오는 경우 문제가 생길 수 있기 때문에 UTF-8로 인코딩을 하도록 설정해준다.
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProperties.bootstrapServers);
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProperties.autoOffsetReset);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "moneyToDisplay");
        return new DefaultKafkaConsumerFactory<>(configMap, new StringDeserializer(), new JsonDeserializer<>(PutMoneyRequest.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PutMoneyRequest> kafkaListenerForBalanceDisplay(
            KafkaProperties.KafkaConsumersProperties consumerProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PutMoneyRequest> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryForBalanceDisplay(consumerProperties));
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, PutMoneyRequest> consumerFactoryForSavingBalance(KafkaProperties.KafkaConsumersProperties consumerProperties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProperties.bootstrapServers);
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProperties.autoOffsetReset);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "moneyToDB");
        return new DefaultKafkaConsumerFactory<>(configMap, new StringDeserializer(), new JsonDeserializer<>(PutMoneyRequest.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PutMoneyRequest> kafkaListenerForSavingBalance(
            KafkaProperties.KafkaConsumersProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, PutMoneyRequest> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryForSavingBalance(properties));
        return listenerContainerFactory;
    }
}
