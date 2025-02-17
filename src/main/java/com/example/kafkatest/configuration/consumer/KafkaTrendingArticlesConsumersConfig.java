package com.example.kafkatest.configuration.consumer;

import avro.articles.TrendingArticles;
import com.example.kafkatest.configuration.properties.KafkaProperties;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTrendingArticlesConsumersConfig {
    @Bean
    public ConsumerFactory<Long, TrendingArticles> trendingArticlesKafkaConsumerFactory(KafkaProperties.KafkaConsumersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "trending.articles.group");
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        // by default, only GenericRecord returned by avro. so we have to set SpecificAvroRecord true
        // value.deserializer.specific.avro.reader = true
        // because TrendingArticles is SpecificRecord
        configMap.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, TrendingArticles> kafkaListenerContainerFactoryForTrendingArticles(KafkaProperties.KafkaConsumersProperties properties) {
        ConcurrentKafkaListenerContainerFactory<Long, TrendingArticles> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(trendingArticlesKafkaConsumerFactory(properties));
        return containerFactory;
    }
}
