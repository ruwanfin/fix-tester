package com.finexa.fixtester.util;

import com.finexa.fixtester.constant.KafkaHeaderKey;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;

/**
 * Utility class for extracting Kafka headers from ConsumerRecord.
 */
public final class KafkaHeaderUtil {

    private KafkaHeaderUtil() {
        // Prevent instantiation
    }

    /**
     * Extracts header value by key from ConsumerRecord.
     *
     * @param record    The Kafka consumer record
     * @param headerKey The header key to extract
     * @return The header value as String, or null if not found
     */
    public static String getHeaderValue(ConsumerRecord<?, ?> record, String headerKey) {
        if (record == null || record.headers() == null || headerKey == null) {
            return null;
        }

        Header header = record.headers().lastHeader(headerKey);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * Extracts header value using KafkaHeaderKey enum.
     *
     * @param record    The Kafka consumer record
     * @param headerKey The header key enum
     * @return The header value as String, or null if not found
     */
    public static String getHeaderValue(ConsumerRecord<?, ?> record, KafkaHeaderKey headerKey) {
        if (headerKey == null) {
            return null;
        }
        return getHeaderValue(record, headerKey.getKey());
    }

    /**
     * Extracts transaction ID from record headers.
     */
    public static String getTransactionId(ConsumerRecord<?, ?> record) {
        return getHeaderValue(record, KafkaHeaderKey.TRANSACTION_ID);
    }

    /**
     * Extracts routing target from record headers.
     */
    public static String getRoutingTarget(ConsumerRecord<?, ?> record) {
        return getHeaderValue(record, KafkaHeaderKey.ROUTING_TARGET);
    }

    /**
     * Extracts tenant code from record headers.
     */
    public static String getTenantCode(ConsumerRecord<?, ?> record) {
        return getHeaderValue(record, KafkaHeaderKey.TENANT_CODE);
    }

    /**
     * Extracts message type from record headers.
     */
    public static String getMessageType(ConsumerRecord<?, ?> record) {
        return getHeaderValue(record, KafkaHeaderKey.MESSAGE_TYPE);
    }

    /**
     * Extracts transaction type from record headers.
     */
    public static String getTxnType(ConsumerRecord<?, ?> record) {
        return getHeaderValue(record, KafkaHeaderKey.TXN_TYPE);
    }
}
