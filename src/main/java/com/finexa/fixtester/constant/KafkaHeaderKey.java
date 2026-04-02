package com.finexa.fixtester.constant;

import lombok.Getter;

/**
 * Kafka header keys used for message routing and metadata.
 */
@Getter
public enum KafkaHeaderKey {
    TRANSACTION_ID("id"),
    TENANT_CODE("tn"),
    MESSAGE_TYPE("mt"),
    TXN_TYPE("tt"),
    EVENT_TIME("et"),
    ROUTING_TARGET("rt");  // Routing target: "ALL" = all consumers, "DB_ONLY" = DB consumer only

    private final String key;

    KafkaHeaderKey(String key) {
        this.key = key;
    }
}
