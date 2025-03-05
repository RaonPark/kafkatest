package com.example.kafkatest.configuration.kafkatopic;

import com.example.Payments;
import com.example.kafkatest.configuration.properties.KafkaTopicNames;
import com.raonpark.RevenueData;
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

    @Bean
    public RetryTopicConfiguration revenueTopicConfig(KafkaTemplate<String, RevenueData> kafkaTemplate) {
        return RetryTopicConfigurationBuilder.newInstance()
                .dltSuffix("-dlt")
                .dltProcessingFailureStrategy(DltStrategy.ALWAYS_RETRY_ON_ERROR)
                .maxAttempts(5)
                .concurrency(3)
                // exponential backoff 를 jitter 를 사용하여 수행한다.
                // 무작위성을 부여하여 동시에 작동하는 DLT 에 대해 부하를 줄일 수 있다.
                // 즉, 서버 내부의 오류가 생겼을 경우 동시에 여러 요청이 왔을 때, 단순하게 동작을 하게 되면
                // 온 모든 요청이 같은 순간에 retry 를 진행할 것이고 이는 서버 내부의 부하를 늘리게 된다.
                // 따라서 랜덤성을 주어서(jitter, 지연변이) 동시에 온 요청들이 서로 다른 backoff 를 가지게 하는 것이다.
                .exponentialBackoff(2000, 2, 20000, true)
                .create(kafkaTemplate);
    }
}
