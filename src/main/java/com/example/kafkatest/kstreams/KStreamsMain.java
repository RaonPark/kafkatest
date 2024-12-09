package com.example.kafkatest.kstreams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KStreamsMain {
    public static Map<String, Object> consumerProperties() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:10000,kafka2:10001,kafka3:10002");

        return configMap;
    }

    public static Map<String, Object> producerProperties() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:10000,kafka2:10001,kafka3:10002");

        return configMap;
    }
    public static void main(String[] args) {
        Properties properties = new Properties();
        try(FileInputStream fis = new FileInputStream("src/main/resources/streams.properties")) {
            properties.load(fis);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "basic-streams");
        StreamsBuilder builder = new StreamsBuilder();
        final String inputTopic = properties.getProperty("basic.input.topic");
        final String outputTopic = properties.getProperty("basic.output.topic");

        final String orderNumberStart = "orderNumber-";
        KStream<String, String> firstStream = builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()));
        // not modifying keys or values
        firstStream.peek((key, value) -> System.out.println("key " + key + " value " + value))
                .filter((key, value) -> value.contains(orderNumberStart))
                .mapValues((value) -> value.substring(value.indexOf("-") + 1))
                .filter((key, value) -> Long.parseLong(value) > 1000)
                .peek((key, value) -> System.out.println("key " + key + " value " + value));
        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), properties);
//        TopicLoader.runProducer();

        KStream<String, String> orderStream = builder.stream("order-topic");
        KStream<String, String> purchaseStream = builder.stream("purchase-topic");

        ValueJoiner<String, String, String> valueJoiner =
                (v1, v2) -> v1.equals(v2) ? "purchased" : "noop";

        orderStream.join(purchaseStream,
                valueJoiner,
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5)),
                StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String()))
                .to("purchased-already-topic");

        JsonDeserializer<Widget> widgetDeserializer = new JsonDeserializer<>(Widget.class, false);
        widgetDeserializer.addTrustedPackages("*");

        Serde<Widget> widgetSerde = Serdes.serdeFrom(new JsonSerializer<>(), widgetDeserializer);

        Reducer<Long> reducer = (value1, value2) -> value1 + value2 + 50;

        StreamsBuilder longStream = new StreamsBuilder();
        KStream<Long, Long> myStream = longStream.stream("long-topic");
        myStream.groupByKey().reduce(reducer, Materialized.with(Serdes.Long(), Serdes.Long()))
                .toStream().to("output-long-topic");
    }
}
