package com.finexa.fixtester.consumer.listener;

import com.finexa.fixtester.util.KafkaHeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Exchange Consumer - Receives only messages with routingTarget=ALL header.
 * <p>
 * This consumer processes only messages that need to be sent to the exchange.
 * Messages with routingTarget=DB_ONLY are filtered out by ExchangeConsumerFilterStrategy.
 * <p>
 * The filtering is done at the container level, so this listener only receives
 * messages that have already passed the filter.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.exchange-consumer.enabled", havingValue = "true", matchIfMissing = true)
public class ExchangeConsumer {

    private final AtomicLong messageCount = new AtomicLong(0);
    private final AtomicLong lastLogTime = new AtomicLong(System.currentTimeMillis());

    @KafkaListener(
            topics = "${app.kafka.topic:to-db}",
            groupId = "${app.kafka.exchange-consumer.group-id:exchange-consumer-group}",
            containerFactory = "exchangeConsumerFactory"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        long count = messageCount.incrementAndGet();

        try {
            // Extract headers
            String txnId = KafkaHeaderUtil.getTransactionId(record);
            String routingTarget = KafkaHeaderUtil.getRoutingTarget(record);
            String tenantCode = KafkaHeaderUtil.getTenantCode(record);
            String msgType = KafkaHeaderUtil.getMessageType(record);

            log.info("[EXCHANGE_CONSUMER] Received: txnId={}, routingTarget={}, tenant={}, msgType={}, partition={}, offset={}",
                    txnId, routingTarget, tenantCode, msgType, record.partition(), record.offset());

            // Process message
            processMessage(record, txnId, routingTarget);

            // Acknowledge
            ack.acknowledge();

            // Log stats every 10 seconds
            logStats(count);

        } catch (Exception e) {
            log.error("[EXCHANGE_CONSUMER] Error processing message: partition={}, offset={}, error={}",
                    record.partition(), record.offset(), e.getMessage(), e);
            // Don't acknowledge - message will be redelivered
        }
    }

    /**
     * Process the message - implement your Exchange/FIX processing logic here.
     */
    private void processMessage(ConsumerRecord<String, String> record, String txnId, String routingTarget) {
        // TODO: Implement actual Exchange/FIX processing logic
        // Example: Extract FIX message and send to exchange

        String payload = record.value();
        log.debug("[EXCHANGE_CONSUMER] Processing payload: txnId={}, payload={} , size={} bytes",
                txnId, payload, payload != null ? payload.length() : 0);

        // Simulate processing time (remove in production)
        // Thread.sleep(1);
    }

    private void logStats(long count) {
        long now = System.currentTimeMillis();
        long lastLog = lastLogTime.get();

        if (now - lastLog >= 10000) { // Every 10 seconds
            if (lastLogTime.compareAndSet(lastLog, now)) {
                log.info("[EXCHANGE_CONSUMER] Stats: totalMessages={}", count);
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
