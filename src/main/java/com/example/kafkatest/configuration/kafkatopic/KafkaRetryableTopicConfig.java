package com.example.kafkatest.configuration.kafkatopic;

import com.example.Payments;
import com.example.kafkatest.configuration.properties.KafkaTopicNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

@Configuration
public class KafkaRetryableTopicConfig {
    @Bean
    public RetryTopicConfiguration paymentsStreamsTopicConfig(KafkaTemplate<String, Payments> kafkaTemplate) {
        return RetryTopicConfigurationBuilder.newInstance()
                .dltSuffix("-dlt")
                .dltProcessingFailureStrategy(DltStrategy.FAIL_ON_ERROR)
                .maxAttempts(1)
                .includeTopic(KafkaTopicNames.PAYMENTS_STREAMS_TOPIC)
                .dltHandlerMethod("retryableTopicDltMethod", "sendErrorPaymentsToDlq")
                .create(kafkaTemplate);
    }
}
