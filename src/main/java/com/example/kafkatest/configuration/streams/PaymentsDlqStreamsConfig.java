package com.example.kafkatest.configuration.streams;

import com.example.Payments;
import com.example.kafkatest.configuration.properties.KafkaTopicNames;
import com.example.kafkatest.support.AvroService;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@EnableKafkaStreams
@EnableKafka
@Slf4j
public class PaymentsDlqStreamsConfig {
    private final PaymentsDlqProcessor paymentsDlqProcessor;
    private final PaymentsDlqCountsProcessor paymentsDlqCountsProcessor;

    @Bean
    public KafkaStreams paymentsDlqStreams() {
        Topology dlqTopology = paymentsDlq();

        HashMap<String, Object> configMap = new HashMap<>();
        configMap.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:9092,kafka2:9092,kafka3:9092");
        configMap.put(StreamsConfig.APPLICATION_ID_CONFIG, "payments-streams-dlq");
        configMap.put(StreamsConfig.CLIENT_ID_CONFIG, "DLQ.STREAMS");
        configMap.put("schema.registry.url", "http://schema-registry:8081");
        configMap.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        configMap.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Properties kafkaStreamsProperties = new Properties();
        kafkaStreamsProperties.putAll(configMap);

        log.info("Topology for payments started: {}", dlqTopology.describe());
        KafkaStreams kafkaStreams = new KafkaStreams(dlqTopology, kafkaStreamsProperties);
        kafkaStreams.start();

        return kafkaStreams;
    }

    private Topology paymentsDlq() {
        Map<String, Object> avroConfigMap = new HashMap<>();
        avroConfigMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        avroConfigMap.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        SpecificAvroSerde<Payments> paymentsSpecificAvroSerde = AvroService.getSpecificAvroSerdeForValue(avroConfigMap);

        // you should configure payments topic as well as dlq topic. so that both producer and consumer may
        // receive/send properly.
        // and you have to receive data from DLQ topic not payments topic itself.
        // since the scenario would look like this one.
        // 1. kafka producer send payments-topic first.
        // 2. kafka consumer received payload from "retryable" payments-topic.
        // 3. kafka would retry polling, but it fails and go to dlq topic.
        // 4. and this stream, which deals with dlq packets, will receive data from retryable payments-topic.
        // 5. finally, this dlq stream fixes message and throw to payments-topic.

        // Build Topology cuz we use processor to change payments value.
        Topology dlqTopology = new Topology();

        dlqTopology.addSource("payments-source",
                new StringDeserializer(),
                paymentsSpecificAvroSerde.deserializer(),
                KafkaTopicNames.PAYMENTS_STREAMS_DLQ_TOPIC);

        dlqTopology.addProcessor("payments-dlq-processor", paymentsDlqProcessor, "payments-source");
        dlqTopology.addProcessor("payments-dlq-counts-processor", paymentsDlqCountsProcessor, "payments-source");

        dlqTopology.addSink("to-payments-topic", KafkaTopicNames.PAYMENTS_STREAMS_TOPIC,
                new StringSerializer(), paymentsSpecificAvroSerde.serializer(),
                "payments-dlq-processor");
        dlqTopology.addSink("to-counts-dlq-topic", KafkaTopicNames.PAYMENTS_STREAMS_DLQ_COUNTS_TOPIC,
                new StringSerializer(), new IntegerSerializer(),
                "payments-dlq-counts-processor");

        return dlqTopology;
    }
}
