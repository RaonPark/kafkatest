package com.example.kafkatest.configuration;

import avro.articles.TrendingArticles;
import com.example.ProblemSolving;
import com.example.kafkatest.support.ProblemSolvingProcessor;
import com.example.kafkatest.support.AvroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
// Spring for Apache Kafka provides the @EnableKafkaStreams annotation,
// which you should place on a @Configuration class.
@EnableKafkaStreams
@EnableKafka
@Slf4j
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
        configMap.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        configMap.put("schema.registry.url", "http://schema-registry:8081");
        configMap.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        configMap.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // JsonDeserializer 설정
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");

        return new KafkaStreamsConfiguration(configMap);
    }


    @Bean
    public KStream<Long, TrendingArticles> trendingArticlesStream(StreamsBuilder streamsBuilder, ObjectMapper objectMapper) {
        // Avro Configuration
        Map<String, Object> avroConfigMap = new HashMap<>();
        avroConfigMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        avroConfigMap.put("specific.avro.reader", true);
        SpecificAvroSerde<TrendingArticles> trendingArticlesAvroSerde = AvroService.getSpecificAvroSerdeForValue(avroConfigMap);

        // Get KStream of trending articles.
        // with 1 hour windowing and grace period 30 seconds
        KStream<Long, TrendingArticles> stream =
                streamsBuilder.stream("articles.topic", Consumed.with(Serdes.Long(), trendingArticlesAvroSerde));
        Duration windowSize = Duration.ofMinutes(3);
        Duration gracePeriod = Duration.ofSeconds(10);

        Aggregator<Long, TrendingArticles, Long> aggregator = (key, value, vAgg) -> vAgg + value.getLikes();

        KTable<Windowed<Long>, Long> likedCountTable =
                stream.groupBy((key, value) -> value.getArticleId(), Grouped.with(Serdes.Long(), trendingArticlesAvroSerde))
                .windowedBy(TimeWindows.ofSizeAndGrace(windowSize, gracePeriod))
                .aggregate(
                        () -> 0L,
                        aggregator,
                        Materialized.<Long, Long, WindowStore<Bytes, byte[]>>as("TRENDING_ARTICLES")
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(Serdes.Long())
                );

        likedCountTable.toStream()
                .map((wk, value) -> KeyValue.pair(wk.key(), new TrendingArticles(wk.key(), value)))
//                .peek((key, value) -> log.info("there's articleId = {} and likes = {}", key, value))
                .to("trending.articles.topic", Produced.with(Serdes.Long(), trendingArticlesAvroSerde));

        stream.print(Printed.toSysOut());

        return stream;
    }

    @Bean
    public Topology problemSolvingStream() {
        Topology topology = buildTopology();

        Map<String, Object> configMap = new HashMap<>();
        // Kafka Streams 설정
        configMap.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
        configMap.put(StreamsConfig.APPLICATION_ID_CONFIG, "problem.solving.app");
        configMap.put(StreamsConfig.CLIENT_ID_CONFIG, "P.S.A");
        configMap.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        configMap.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        configMap.put("schema.registry.url", "http://schema-registry:8081");
        configMap.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        configMap.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // JsonDeserializer 설정
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configMap.put("spring.kafka.consumer.properties.spring.json.encoding", "UTF-8");

        Properties kafkaStreamsProperties = new Properties();
        kafkaStreamsProperties.putAll(configMap);

        final KafkaStreams kafkaStreams = new KafkaStreams(topology, kafkaStreamsProperties);
        kafkaStreams.start();

        return topology;
    }

    private Topology buildTopology() {
        final Topology topology = new Topology();
        final Map<String, Object> configMap =
                new HashMap<>(Collections.singletonMap("schema.registry.url", "http://schema-registry:8081"));
        configMap.put("specific.avro.reader", true);
        final SpecificAvroSerde<ProblemSolving> problemSolvingSpecificAvroSerde =
                AvroService.getSpecificAvroSerdeForValue(configMap);

        topology.addSource("source-node",
                Serdes.String().deserializer(),
                problemSolvingSpecificAvroSerde.deserializer(),
                "solving.problem.topic");

        topology.addProcessor("processor-node",
                new ProblemSolvingProcessor(),
                "source-node");

        topology.addSink("sink-node",
                "solved.problem.topic",
                Serdes.String().serializer(),
                Serdes.Integer().serializer(),
                "processor-node");

        return topology;
    }
}
