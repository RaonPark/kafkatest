package com.example.kafkatest.kstreams;

import com.example.ProblemSolving;
import com.example.kafkatest.support.AvroService;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class StreamsTimeMain {

    static class ProblemSolvedTimestampExtractor implements TimestampExtractor {
        @Override
        public long extract(ConsumerRecord<Object, Object> consumerRecord, long l) {
            ProblemSolving problem = (ProblemSolving) consumerRecord.value();
            return problem.getSolvedTime();
        }
    }

    public static void main(String[] args) {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        SpecificAvroSerde<ProblemSolving> problemSpecificAvroSerde = AvroService.getSpecificAvroSerdeForValue(configMap);

        final KStream<String, ProblemSolving> myStream = streamsBuilder.stream("problem.solve.topic",
                 Consumed.with(Serdes.String(), problemSpecificAvroSerde)
                         .withTimestampExtractor(new ProblemSolvedTimestampExtractor()));

        final KTable<Windowed<String>, Integer> myKTable = myStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .aggregate(
                        () -> 0,
                        (key, value, solved) -> solved + value.getSolved(),
                        Materialized.<String, Integer, WindowStore<Bytes, byte[]>>as("PROBLEM-SOLVING")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Integer())
                );

        myKTable.toStream().map((wk, value) -> new KeyValue<>(wk.key(), value))
                .peek((key, value) -> System.out.println("here is userId = " + key + " and solved problems " + value))
                .to("problem.solved.topic", Produced.with(Serdes.String(), Serdes.Integer()));
    }
}
