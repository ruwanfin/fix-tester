package com.finexa.fixtester.consumer.health;

import com.finexa.fixtester.consumer.listener.DbConsumer;
import com.finexa.fixtester.consumer.listener.ExchangeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Health indicator that logs consumer statistics periodically.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ConsumerHealthIndicator {

    private final DbConsumer dbConsumer;
    private final ExchangeConsumer exchangeConsumer;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("==============================================");
        log.info("  FINEXA FIX TESTER APPLICATION STARTED");
        log.info("==============================================");
        log.info("  FIX Client UI: http://localhost:8091");
        log.info("  DB Consumer: ACTIVE (receives ALL messages)");
        log.info("  Exchange Consumer: ACTIVE (receives only routingTarget=ALL)");
        log.info("==============================================");
    }

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void logHealthStatus() {
        log.info("=== CONSUMER HEALTH STATUS ===");
        log.info("  DB Consumer - Messages processed: {}", dbConsumer.getMessageCount());
        log.info("  Exchange Consumer - Messages processed: {}", exchangeConsumer.getMessageCount());
        log.info("==============================");
    }
}
