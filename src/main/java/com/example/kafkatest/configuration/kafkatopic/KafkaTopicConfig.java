package com.example.kafkatest.configuration.kafkatopic;

import com.example.kafkatest.configuration.properties.KafkaTopicNames;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Value("${spring.kafka.admin.properties.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.admin.properties.security-protocol}")
    private String securityProtocol;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configMap.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        return new KafkaAdmin(configMap);
    }
    /*
    compact()를 한 토픽은 키 없이는 메세지를 보낼 수 없다.
    Compacted topic cannot accept message without key in topic partition testTopic-5
     */
    @Bean
    public NewTopic testTopic() {
        return TopicBuilder
                .name("testTopic")
                .replicas(2)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .build();
    }

    @Bean
    public NewTopic balanceTopic() {
        return TopicBuilder
                .name("PutMoney")
                .replicas(2)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .build();
    }

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder
                .name("chat")
                .replicas(2)
                .partitions(5)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .build();
    }

    @Bean
    public NewTopic kvswTopic() {
        return TopicBuilder
                .name("kvsw")
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .build();
    }

    @Bean
    public NewTopic keywordsTopic() {
        return TopicBuilder
                .name("keywords")
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic articlesTopic() {
        return TopicBuilder
                .name("articles.topic")
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic trendingArticlesTopic() {
        return TopicBuilder
                .name("trending.articles.topic")
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic solvingProblemTopic() {
        return TopicBuilder
                .name("solving.problem.topic")
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic solvedProblemTopic() {
        return TopicBuilder
                .name("solved.problem.topic")
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic paymentsTopic() {
        return TopicBuilder
                .name("payments.topic")
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic paymentsStreamsTopic() {
        return TopicBuilder
                .name(KafkaTopicNames.PAYMENTS_STREAMS_TOPIC)
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic paymentsDlqTopic() {
        return TopicBuilder
                .name(KafkaTopicNames.PAYMENTS_STREAMS_DLQ_TOPIC)
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }

    @Bean
    public NewTopic paymentsDlqCountsTopic() {
        return TopicBuilder
                .name(KafkaTopicNames.PAYMENTS_STREAMS_DLQ_COUNTS_TOPIC)
                .replicas(3)
                .partitions(10)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer")
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }
}