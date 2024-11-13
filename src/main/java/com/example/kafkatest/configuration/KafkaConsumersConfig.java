package com.example.kafkatest.configuration;

import com.example.kafkatest.dto.request.PutMoneyRequest;
import com.example.kafkatest.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
// @ConfigurationProperties가 붙은 경우는 기본적으로 외부 value값을 통해 생성자를 초기화해줘야 하는데
// 문제는 @Configuration이 붙으면 스프링 bean이 되는데 스프링에서 bean으로 다뤄야하는지 외부 값에 의해 바인딩 되므로 빈으로 다뤄야하지 않는지
// 모호해지기 때문에 오류가 날 수 있다. 따라서 클래스를 분리하자.
@EnableConfigurationProperties(KafkaConsumersConfig.KafkaConsumerProperties.class)
public class KafkaConsumersConfig {

    @RequiredArgsConstructor
    @ConfigurationProperties(prefix = "spring.kafka.consumer")
    public static class KafkaConsumerProperties {
        private final String bootstrapServers;
        private final Class<StringDeserializer> keyDeserializer;
        private final Class<StringDeserializer> valueDeserializer;
        private final String autoOffsetReset;
        private final boolean enableAutoCommit;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryKafkatest(KafkaConsumerProperties consumerProperties) {
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
            KafkaConsumerProperties consumerProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryKafkatest(consumerProperties));
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryAnotherId(KafkaConsumerProperties consumerProperties) {
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
            KafkaConsumerProperties consumerProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryKafkatest(consumerProperties));
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, PutMoneyRequest> consumerFactoryForBalanceDisplay(KafkaConsumerProperties consumerProperties) {
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
            KafkaConsumerProperties consumerProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PutMoneyRequest> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryForBalanceDisplay(consumerProperties));
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, PutMoneyRequest> consumerFactoryForSavingBalance(KafkaConsumerProperties consumerProperties) {
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
            KafkaConsumerProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, PutMoneyRequest> listenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactoryForSavingBalance(properties));
        return listenerContainerFactory;
    }

    @Bean
    public ConsumerFactory<String, ChatMessage> chatConsumerFactory(KafkaConsumerProperties properties) {
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
            KafkaConsumerProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessage> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(chatConsumerFactory(properties));
        return containerFactory;
    }
}
