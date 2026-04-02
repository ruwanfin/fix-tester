package com.finexa.test.ordergrpcclienttester.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    @Bean
    public ManagedChannel orderServiceChannel() {
        return ManagedChannelBuilder
                .forAddress("localhost", 9092)
                .usePlaintext()
                .build();
    }
}
