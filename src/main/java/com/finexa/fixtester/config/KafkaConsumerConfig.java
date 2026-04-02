package com.finexa.fixtester.config;

import com.finexa.fixtester.consumer.filter.ExchangeConsumerFilterStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration with filtering support.
 * <p>
 * Provides two container factories:
 * <ul>
 *   <li>dbConsumerFactory - Default factory, no filtering (for DB consumer)</li>
 *   <li>exchangeConsumerFactory - With header-based filtering (for Exchange consumer)</li>
 * </ul>
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.max-poll-records:500}")
    private int maxPollRecords;

    @Value("${app.kafka.db-consumer.concurrency:3}")
    private int dbConsumerConcurrency;

    @Value("${app.kafka.exchange-consumer.concurrency:3}")
    private int exchangeConsumerConcurrency;

    // =====================================================================
    // Consumer Factory (shared)
    // =====================================================================

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        log.info("Creating Kafka ConsumerFactory with bootstrap servers: {}", bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // =====================================================================
    // DB Consumer Factory - No Filter (receives ALL messages)
    // =====================================================================

    @Bean("dbConsumerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> dbConsumerFactory(
            ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(dbConsumerConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        log.info("Created DB Consumer Factory with concurrency: {}", dbConsumerConcurrency);
        return factory;
    }

    // =====================================================================
    // Exchange Consumer Factory - With Header Filter
    // =====================================================================

    @Bean("exchangeConsumerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> exchangeConsumerFactory(
            ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(exchangeConsumerConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Set the filter strategy - only messages with routingTarget=ALL are processed
        factory.setRecordFilterStrategy(new ExchangeConsumerFilterStrategy());

        // Acknowledge filtered (discarded) records to advance offsets
        factory.setAckDiscarded(true);

        log.info("Created Exchange Consumer Factory with concurrency: {} and routing filter enabled",
                exchangeConsumerConcurrency);
        return factory;
    }
}
