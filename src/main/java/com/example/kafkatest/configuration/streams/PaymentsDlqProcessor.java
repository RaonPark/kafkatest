package com.example.kafkatest.configuration.streams;

import com.example.Payments;
import com.example.kafkatest.support.AvroService;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class PaymentsDlqProcessor implements ProcessorSupplier<String, Payments, String, Payments> {
    @Override
    public Processor<String, Payments, String, Payments> get() {
        return new Processor<>() {
            private ProcessorContext<String, Payments> processorContext;
            private KeyValueStore<String, Payments> store;

            @Override
            public void init(ProcessorContext<String, Payments> context) {
                this.processorContext = context;
                this.store = context.getStateStore("Payments-Dlq");
                this.processorContext.schedule(Duration.ofMinutes(2), PunctuationType.STREAM_TIME, timestamp -> {
                    log.info("dlq processor timestamp = {}, now time = {}", Instant.ofEpochMilli(timestamp), Instant.now());
                    try(final KeyValueIterator<String, Payments> iter = store.all()) {
                        while(iter.hasNext()) {
                            final KeyValue<String, Payments> keyValue = iter.next();
                            processorContext.forward(new Record<>(keyValue.key, keyValue.value, timestamp));
                        }
                    }
                });
            }

            @Override
            public void process(Record<String, Payments> record) {
                final Payments payment = record.value();

                if(payment.getPaymentsId().toString().contains("error")) {
                    Payments corrected = Payments.newBuilder()
                            .setPaymentsId("corrected-payments")
                            .setCurrency(payment.getCurrency())
                            .setAmount(payment.getAmount())
                            .setUser(payment.getUser())
                            .setIsCredit(payment.getIsCredit())
                            .setPaymentsStamp(Instant.ofEpochMilli(record.timestamp()))
                            .build();
                    store.put(record.key(), corrected);
                }
            }
        };
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        configMap.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        SpecificAvroSerde<Payments> avroSerde = AvroService.getSpecificAvroSerdeForValue(configMap);
        return Set.of(Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("Payments-Dlq"),
                Serdes.String(),
                avroSerde
        ));
    }
}
