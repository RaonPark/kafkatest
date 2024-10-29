package com.example.kafkatest.configuration;

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

}
