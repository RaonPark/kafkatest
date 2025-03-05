package com.example.kafkatest.configuration.consumer;

import com.example.kafkatest.configuration.properties.KafkaProperties;
import com.raonpark.RevenueData;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaRevenueDataConsumer {
    @Bean
    public ConsumerFactory<String, RevenueData> consumerFactory(KafkaProperties.KafkaConsumersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        configMap.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "REVENUE");
        // new consumer protocol, after KIP-848 which is KRaft based.
        configMap.put(ConsumerConfig.GROUP_PROTOCOL_CONFIG, "CONSUMER");
        configMap.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 5000);
        // MAX amount of data per-partition the server will return.
        configMap.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 10 * 1024 * 1024);
        // This configuration must be set to `false` when using brokers older than 0.11.0
        // I use the newest version of Kafka, so it could be `yes`
        // if not topic exists. kafka will create the topic for you with default settings.
        // so I set this value as false
        configMap.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false);
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.enableAutoCommit);
        // set kafka offset as earliest when there is no initial offset
        // so, if the first time that consumer consumes record, it will consume the `earliest` offset of queue
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RevenueData> kafkaListenerContainerFactory(KafkaProperties.KafkaConsumersProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, RevenueData> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory(properties));

        return containerFactory;
    }
}
