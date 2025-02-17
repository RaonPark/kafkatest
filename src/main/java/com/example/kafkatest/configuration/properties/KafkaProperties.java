package com.example.kafkatest.configuration.properties;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
// @ConfigurationProperties가 붙은 경우는 기본적으로 외부 value값을 통해 생성자를 초기화해줘야 하는데
// 문제는 @Configuration이 붙으면 스프링 bean이 되는데 스프링에서 bean으로 다뤄야하는지 외부 값에 의해 바인딩 되므로 빈으로 다뤄야하지 않는지
// 모호해지기 때문에 오류가 날 수 있다. 따라서 클래스를 분리하자.
@EnableConfigurationProperties({KafkaProperties.KafkaProducersProperties.class, KafkaProperties.KafkaConsumersProperties.class})
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

    @RequiredArgsConstructor
    @ConfigurationProperties(prefix = "spring.kafka.consumer")
    public static class KafkaConsumersProperties {
        public final String bootstrapServers;
        public final Class<StringDeserializer> keyDeserializer;
        public final Class<StringDeserializer> valueDeserializer;
        public final String autoOffsetReset;
        public final boolean enableAutoCommit;
        public final List<String> groupIds;
    }
}
