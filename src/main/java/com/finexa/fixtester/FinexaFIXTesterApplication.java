package com.finexa.fixtester;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Finexa FIX Tester Application.
 * <p>
 * Combined application that provides:
 * <ul>
 *   <li>FIX Client UI for sending test messages to the Order Management System</li>
 *   <li>Kafka consumers for receiving and processing messages</li>
 * </ul>
 * <p>
 * Features:
 * <ul>
 *   <li>Producer: Sends FIX protocol messages to Kafka topic (from-exchange)</li>
 *   <li>DB Consumer: Receives ALL messages from topic (to-db)</li>
 *   <li>Exchange Consumer: Receives only messages with routingTarget=ALL header</li>
 * </ul>
 */
@Slf4j
@SpringBootApplication
public class FinexaFIXTesterApplication {

    public static void main(String[] args) {
        log.info("Starting Finexa FIX Tester Application...");
        SpringApplication.run(FinexaFIXTesterApplication.class, args);
        log.info("Finexa FIX Tester Application started successfully!");
    }
}
