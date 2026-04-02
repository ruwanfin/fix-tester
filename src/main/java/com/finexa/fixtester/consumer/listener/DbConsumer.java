package com.finexa.fixtester.consumer.listener;

import com.finexa.fixtester.util.KafkaHeaderUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * DB Consumer - Receives ALL messages from the topic (no filtering).
 * <p>
 * This consumer processes every message regardless of the routingTarget header.
 * It's responsible for persisting data to the database.
 */
@Log4j2
@Component
@ConditionalOnProperty(name = "app.kafka.db-consumer.enabled", havingValue = "true", matchIfMissing = true)
public class DbConsumer {

    private final AtomicLong messageCount = new AtomicLong(0);
    private final AtomicLong lastLogTime = new AtomicLong(System.currentTimeMillis());

    @KafkaListener(
            topics = "${app.kafka.topic:to-db}",
            groupId = "${app.kafka.db-consumer.group-id:db-consumer-group}",
            containerFactory = "dbConsumerFactory"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        long count = messageCount.incrementAndGet();

        try {
            // Extract headers
            String txnId = KafkaHeaderUtil.getTransactionId(record);
            String routingTarget = KafkaHeaderUtil.getRoutingTarget(record);
            String tenantCode = KafkaHeaderUtil.getTenantCode(record);
            String msgType = KafkaHeaderUtil.getMessageType(record);

            log.info("[DB_CONSUMER] Received: txnId={}, routingTarget={}, tenant={}, msgType={}, partition={}, offset={}",
                    txnId, routingTarget, tenantCode, msgType, record.partition(), record.offset());

            // Process message
            processMessage(record, txnId, routingTarget);

            // Acknowledge
            ack.acknowledge();

            // Log stats every 10 seconds
            logStats(count);

        } catch (Exception e) {
            log.error("[DB_CONSUMER] Error processing message: partition={}, offset={}, error={}",
                    record.partition(), record.offset(), e.getMessage(), e);
            // Don't acknowledge - message will be redelivered
        }
    }

    /**
     * Process the message - implement your DB persistence logic here.
     */
    private void processMessage(ConsumerRecord<String, String> record, String txnId, String routingTarget) {
        // TODO: Implement actual DB processing logic
        // Example: Parse payload and save to database

        String payload = record.value();
        log.debug("[DB_CONSUMER] Processing payload: txnId={}, payload={} , size={} bytes",
                txnId, payload, payload != null ? payload.length() : 0);

        // Simulate processing time (remove in production)
        // Thread.sleep(1);
    }

    private void logStats(long count) {
        long now = System.currentTimeMillis();
        long lastLog = lastLogTime.get();

        if (now - lastLog >= 10000) { // Every 10 seconds
            if (lastLogTime.compareAndSet(lastLog, now)) {
                log.info("[DB_CONSUMER] Stats: totalMessages={}", count);
            }
        }
    }

    public long getMessageCount() {
        return messageCount.get();
    }

    public void resetCount() {
        messageCount.set(0);
    }
}
