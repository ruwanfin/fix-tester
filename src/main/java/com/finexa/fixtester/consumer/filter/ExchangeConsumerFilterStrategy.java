package com.finexa.fixtester.consumer.filter;

import com.finexa.fixtester.constant.RoutingTarget;
import com.finexa.fixtester.util.KafkaHeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

/**
 * Filter strategy for Exchange consumer.
 * <p>
 * This filter allows only messages with routingTarget=ALL header to pass through.
 * Messages with routingTarget=DB_ONLY or missing routing header are discarded.
 * <p>
 * How it works:
 * <ul>
 *   <li>Returns {@code true} to DISCARD the message (filter out)</li>
 *   <li>Returns {@code false} to KEEP the message (pass through)</li>
 * </ul>
 */
@Slf4j
public class ExchangeConsumerFilterStrategy implements RecordFilterStrategy<String, String> {

    @Override
    public boolean filter(ConsumerRecord<String, String> record) {
//        String routingTarget = KafkaHeaderUtil.getRoutingTarget(record);
//        String txnId = KafkaHeaderUtil.getTransactionId(record);
//
//        // Return true to DISCARD, false to KEEP
//        // Keep only messages with routingTarget = "ALL"
//        boolean shouldDiscard = !RoutingTarget.ALL.equals(routingTarget);
//
//        if (shouldDiscard) {
//            log.debug("EXCHANGE_FILTER: Discarding message - txnId={}, routingTarget={}, partition={}, offset={}",
//                    txnId, routingTarget, record.partition(), record.offset());
//        } else {
//            log.debug("EXCHANGE_FILTER: Accepting message - txnId={}, routingTarget={}, partition={}, offset={}",
//                    txnId, routingTarget, record.partition(), record.offset());
//        }

        return true;
    }
}
