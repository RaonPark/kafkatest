package com.example.kafkatest.support;

import com.example.ProblemSolving;
import com.example.kafkatest.support.LinearRegressionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class ProblemSolvingProcessor implements ProcessorSupplier<String, ProblemSolving, String, Integer> {
    private final String storeName = "PROBLEM_SOLVING";

    @Override
    public Processor<String, ProblemSolving, String, Integer> get() {
        return new Processor<>() {
            private ProcessorContext<String, Integer> context;
            private KeyValueStore<String, Integer> store;

            @Override
            public void init(ProcessorContext<String, Integer> context) {
                this.context = context;
                store = context.getStateStore(storeName);
                this.context.schedule(Duration.ofMinutes(2), PunctuationType.STREAM_TIME, this::updateProblems);
            }

            private void updateProblems(final long timestamp) {
                log.info("in updateProblems timestamp : {}", timestamp);
                try(KeyValueIterator<String, Integer> iterator = store.all()) {
                    while (iterator.hasNext()) {
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
                if(solvedProblems == null)
                    solvedProblems = 0;

                Integer newSolvedProblems = solvedProblems + record.value().getSolved();
                store.put(key, newSolvedProblems);
            }
        };
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(storeName),
                        Serdes.String(),
                        Serdes.Integer()
                )
        );
    }
}
