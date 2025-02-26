package com.example.kafkatest.configuration.streams;

import com.example.Payments;
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
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class PaymentsDlqCountsProcessor implements ProcessorSupplier<String, Payments, String, Integer> {
    @Override
    public Processor<String, Payments, String, Integer> get() {
        return new Processor<String, Payments, String, Integer>() {
            private ProcessorContext<String, Integer> countsContext;
            private KeyValueStore<String, Integer> kvStore;
            @Override
            public void init(ProcessorContext<String, Integer> context) {
                this.countsContext = context;
                // materialized key value store.
                // like table, we can make store name. with Payments-Dlq-Counts.
                this.kvStore = context.getStateStore(KafkaKeyValueStoreNames.PAYMENTS_DLQ_COUNTS);
                countsContext.schedule(Duration.ofMinutes(5), PunctuationType.WALL_CLOCK_TIME, timestamp -> {
                    try(final KeyValueIterator<String, Integer> iter = kvStore.all()) {
                        while(iter.hasNext()) {
                            final KeyValue<String, Integer> kv = iter.next();
                            countsContext.forward(new Record<>(kv.key, kv.value, timestamp));
                        }
                    }
                });
            }

            @Override
            public void process(Record<String, Payments> record) {
                final Payments errorPayments = record.value();

                if(errorPayments.getPaymentsId().toString().contains("error")) {
                    // we have to deal with null pointer exception.
                    // a store is key-value map. a value which is mapped by the key might not exist.
                    int dlqCounts = Optional.ofNullable(kvStore.get(record.key())).orElse(0);
                    kvStore.put(record.key(), dlqCounts + 1);
                }
            }
        };
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(KafkaKeyValueStoreNames.PAYMENTS_DLQ_COUNTS),
                Serdes.String(),
                Serdes.Integer()
        ));
    }
}
