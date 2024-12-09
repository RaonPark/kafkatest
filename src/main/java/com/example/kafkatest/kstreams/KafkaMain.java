package com.example.kafkatest.kstreams;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KafkaMain {
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
        try(Consumer<String, Widget> consumer = new KafkaConsumer<>(consumerProperties());
            Producer<String, Widget> producer = new KafkaProducer<>(producerProperties())) {
            consumer.subscribe(Collections.singletonList("widgets"));
            while(true) {
                ConsumerRecords<String, Widget> records = consumer.poll(Duration.ofSeconds(5));
                for(ConsumerRecord<String, Widget> record : records) {
                    Widget widget = record.value();

                    if(widget.getColour().equals("red")) {
                        ProducerRecord<String, Widget> producerRecord = new ProducerRecord<>("widgets-red",
                                record.key(), widget);
                        producer.send(producerRecord, ((recordMetadata, e) -> {}));
                    }
                }
            }
        }
    }
}
