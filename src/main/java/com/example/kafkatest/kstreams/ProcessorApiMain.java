package com.example.kafkatest.kstreams;

import com.example.ProblemSolving;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.time.Duration;
import java.util.Properties;
import java.util.Set;

public class ProcessorApiMain {
    static class ProblemSolvingProcessorSupplier implements ProcessorSupplier<String, ProblemSolving, String, Integer> {
        final String storeName;
        public ProblemSolvingProcessorSupplier(String storeName) {
            this.storeName = storeName;
        }
        @Override
        public Processor<String, ProblemSolving, String, Integer> get() {
            return new Processor<>() {
                private ProcessorContext<String, Integer> context;
                private KeyValueStore<String, Integer> store;

                @Override
                public void init(ProcessorContext<String, Integer> context) {
                    this.context = context;
                    store = context.getStateStore(storeName);
                    this.context.schedule(Duration.ofSeconds(30), PunctuationType.STREAM_TIME, this::forwardAll);
                }

                private void forwardAll(final long timestamp) {
                    try(KeyValueIterator<String, Integer> iterator = store.all()) {
                        while(iterator.hasNext()) {
                            final KeyValue<String, Integer> nextKV = iterator.next();
                            final Record<String, Integer> record = new Record<>(nextKV.key, nextKV.value, timestamp);
                            context.forward(record);
                        }
                    }
                }

                @Override
                public void process(Record<String, ProblemSolving> record) {
                    final String key = record.key();
                    Integer solvedProblems = store.get(key);
                    if(solvedProblems == null) {
                        solvedProblems = 0;
                    }

                    Integer newSolvedProblems = record.value().getSolved() + solvedProblems;
                    store.put(key, newSolvedProblems);
                }
            };
        }

        @Override
        public Set<StoreBuilder<?>> stores() {
            return ProcessorSupplier.super.stores();
        }
    }

    final static String storeName = "solved-problems-store";
    static StoreBuilder<KeyValueStore<String, Integer>> totalSolvedProblemsStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(storeName),
            Serdes.String(),
            Serdes.Integer()
    );
    public static void main(String[] args) {
        final Topology topology = new Topology();

        topology.addSource("source-node",
                Serdes.String().deserializer(),
                Serdes.Integer().deserializer(),
                "problem-solving-topic");

        topology.addProcessor(
                "aggregate-solved-problems",
                new ProblemSolvingProcessorSupplier(storeName),
                "source-node"
        );

        topology.addSink("sink-node",
                "problem-solved-topic",
                Serdes.String().serializer(),
                Serdes.Integer().serializer());

        final KafkaStreams kafkaStreams = new KafkaStreams(topology, new Properties());
        kafkaStreams.start();
    }
}
