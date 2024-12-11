package com.example.kafkatest.configuration;

import com.example.kafkatest.entity.Articles;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
// Spring for Apache Kafka provides the @EnableKafkaStreams annotation,
// which you should place on a @Configuration class.
@EnableKafkaStreams
public class KafkaStreamsConfig {

    // StreamsBuilder 를 생성해준다.
    // 혹은 FactoryBean<StreamsBuilder> 타입의 빈을 생성할 수도 있음.
    // @Bean(name = ...) 으로 StreamsBuilder 가 아니라 설정의 빈임을 명시해준다.
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
        Map<String, Object> configMap = new HashMap<>();
        // Kafka Streams 설정
        configMap.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
        configMap.put(StreamsConfig.APPLICATION_ID_CONFIG, "trending.articles.app");
        configMap.put(StreamsConfig.CLIENT_ID_CONFIG, "T.A.C");
        configMap.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        configMap.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
        configMap.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        configMap.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);

        // JsonDeserializer 설정
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");

        return new KafkaStreamsConfiguration(configMap);
    }


    @Bean
    public KStream<String, String> likedArticleStream(StreamsBuilder streamsBuilder, ObjectMapper objectMapper) {
        KStream<String, String> stream = streamsBuilder.stream("liked-topic");

        return stream;
    }
}
