package com.example.kafkatest.configuration.consumer;

import com.example.kafkatest.configuration.properties.KafkaProperties;
import com.example.kafkatest.entity.document.OrderPaymentOutbox;
import com.raonpark.OrderPaymentOutboxAvro;
import com.raonpark.PaymentData;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaOrderPaymentOutboxConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderPaymentOutboxAvro> orderPaymentOutboxConsumerFactory(KafkaProperties.KafkaConsumersProperties properties) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        configMap.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema-registry:8081");
        configMap.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, "order-payment-outbox");
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.enableAutoCommit);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaymentOutboxAvro> orderPaymentOutboxConcurrentKafkaListenerContainerFactory(
            KafkaProperties.KafkaConsumersProperties properties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPaymentOutboxAvro> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(orderPaymentOutboxConsumerFactory(properties));
        return containerFactory;
    }
}
