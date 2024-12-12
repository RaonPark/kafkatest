package com.example.kafkatest.support;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AvroService {
    public static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerdeForValue(Map<String, Object> configMap) {
        SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
        // true 면 key 를 위한 serde, false 면 value 를 위한 serde
        specificAvroSerde.configure(configMap, false);
        return specificAvroSerde;
    }
}
