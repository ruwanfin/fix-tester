package com.finexa.test.ordergrpcclienttester.controller;

import com.finexa.orderManage.core.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootApplication
public class OrderGrpcClientTesterLocal implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(OrderGrpcClientTesterLocal.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // -------------------------
        // TEST CONFIG
        // -------------------------
        String target = "localhost:9095";
       // String target = "localhost:9092";
        int totalOrders = 1;
        int concurrency = 1;

        // -------------------------
        // gRPC SETUP
        // -------------------------

        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(target)
                .usePlaintext()
                .build();

        OrderServiceGrpc.OrderServiceBlockingStub stub =
                OrderServiceGrpc.newBlockingStub(channel);

//        // Add warmup before starting the test
//        log.info("🔥 Warming up gRPC connection...");
//        try {
//            OrderRequestGRPC warmupRequest = OrderRequestGRPC.newBuilder()
//                    .setHeader(FineXaMsgHeaderGRPC.newBuilder()
//                            .setUnqReqId("WARMUP")
//                            .setProduct(1)
//                            .setTenantCode("DEFAULT_TENANT")
//                            .setLoginId(1001)
//                            .build())
//                    .setServiceType(2)
//                    .setTimestamp(System.currentTimeMillis())
//                    .setData(OrderRequestBodyGRPC.newBuilder()
//                            .setSymbol("WARMUP")
//                            .setQuantity(1)
//                            .build())
//                    .build();
//
//            stub.processOrder(warmupRequest);
//            log.info("✅ Warmup complete");
//        } catch (Exception e) {
//            log.warn("Warmup failed: {}", e.getMessage());
//        }

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);

        // -------------------------
        // METRICS
        // -------------------------
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        AtomicLong totalRttMs = new AtomicLong(0);
        ConcurrentMap<Long, AtomicInteger> latencyBuckets = new ConcurrentHashMap<>();

        long testStart = System.currentTimeMillis();

        log.info("🚀 Starting gRPC load test ({} orders, concurrency={})",
                totalOrders, concurrency);

        List<Future<?>> futures = new ArrayList<>();

        // -------------------------
        // SUBMIT TASKS
        // -------------------------
        for (int i = 1; i <= totalOrders; i++) {
            int index = i;

            futures.add(executor.submit(() -> {

                // -------------------------
                // HEADER
                // -------------------------
//                FineXaMsgHeaderGRPC header = FineXaMsgHeaderGRPC.newBuilder()
//                        .setProduct(1)
//                        .setTenantCode("DEFAULT_TENANT")
//                        .setLoginId(1001)
//                        .setClientIp("127.0.0.1")
//                        .setSessionId("S-" + index)
//                        .setComponentId("ORDER-TESTER")
//                        .setUnqReqId("REQ-" + index)
//                        .build();

                // -------------------------
                // REQUEST BODY
                // -------------------------
//                OrderRequestBodyGRPC body = OrderRequestBodyGRPC.newBuilder()
//                        .setSymbol("1010")
//                        .setQuantity(100 + index)
//                        .setPrice(150.50)
//                        .setSide(1)
//                        .setType("2")
//                        .setTradingAccountID(78) // no holding 78, holding 23
//                        .setOrdCat(1)
//                        .setExchange("TDWL")
//                        .setType("2")
//                        .build();

                FineXaMsgHeaderGRPC header = FineXaMsgHeaderGRPC.newBuilder()
                        .setProduct(1)
                        .setTenantCode("DEFAULT_TENANT")
                        .setLoginId(6)
                        .setClientIp("127.0.0.1")
                        .setSessionId("S-" + index)
                        .setComponentId("ORDER-TESTER")
                        .setUnqReqId("REQ-" + index)
                        .build();

                OrderRequestBodyGRPC body = OrderRequestBodyGRPC.newBuilder()
                        .setSymbol("1010")              // 1
                        .setPrice(20.0)                 // 2
                        .setTradingAccountID(20)        // 3
                        .setExchange("TDWL")            // 4
                        .setType("2")                   // 5
                        .setSide(1)                     // 6
                        .setQuantity(1000 + index)      // 7
                        .setTif(0)                      // 8
                        .setTradeDate("2026-03-09")     // 9
                        .setCustomerNo("CUST-001")      // 10
                        .setClOrdId("CLORD-" + index)   // 11
                        .setOrderMode(0)                // 12
                        .setOrdCat(1)                   // 13
                        .setBypassRms(0)                // 14
                        .setRemark("test")              // 15
                        .setExecBrokerID(1)             // 16
                        .setCustodianID(1)              // 17
                        .build();

                // -------------------------
                // REQUEST WRAPPER
                // -------------------------
                OrderRequestGRPC request = OrderRequestGRPC.newBuilder()
                        .setHeader(header)
                        .setServiceType(2)
                        .setTimestamp(System.currentTimeMillis())
                        .setData(body)
                        .build();

                try {
                    long rttStartNs = System.nanoTime();

                    log.info("➡️ Sending order start [{}]",
                            request.getHeader().getUnqReqId());

                    OrderResponseGRPC response = stub
                            .withDeadlineAfter(60, TimeUnit.SECONDS)
                            .processOrder(request);

                    long rttEndNs = System.nanoTime();
                    long rttMs = TimeUnit.NANOSECONDS.toMillis(rttEndNs - rttStartNs);

                    log.info("⬅️ Sending order end [{}] -> {} ms",
                            request.getHeader().getUnqReqId(), rttMs);

                    // -------------------------
                    // METRICS UPDATE
                    // -------------------------
                    successCount.incrementAndGet();
                    totalRttMs.addAndGet(rttMs);

                    latencyBuckets
                            .computeIfAbsent(rttMs, k -> new AtomicInteger(0))
                            .incrementAndGet();

                    log.info("✅ [{}] OK → Status: {}, Desc: {}",
                            request.getHeader().getUnqReqId(),
                            response.getData().getStatus(),
                            response.getData().getStatusDescription());

                } catch (StatusRuntimeException e) {
                    errorCount.incrementAndGet();
                    log.error("❌ [{}] gRPC Error: {}",
                            request.getHeader().getUnqReqId(),
                            e.getStatus(), e);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("❌ [{}] Unexpected Error: {}",
                            request.getHeader().getUnqReqId(),
                            e.getMessage(), e);
                }
            }));
        }

        // -------------------------
        // WAIT FOR COMPLETION
        // -------------------------
        for (Future<?> f : futures) {
            try {
                f.get(60, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("⚠️ Timeout waiting for worker");
            } catch (Exception e) {
                log.warn("⚠️ Worker execution failed: {}", e.getMessage());
            }
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long testDurationMs = System.currentTimeMillis() - testStart;
        double throughput =
                (successCount.get() * 1000.0) / testDurationMs;

        double avgRtt =
                successCount.get() == 0
                        ? 0
                        : (double) totalRttMs.get() / successCount.get();

        // -------------------------
        // FINAL REPORT
        // -------------------------

        latencyBuckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e ->
                        log.info("   {} ms -> {} requests",
                                e.getKey(), e.getValue().get())
                );

        log.info("\n🏁 Load Test Complete");
        log.info("   ✅ Success      : {}", successCount.get());
        log.info("   ❌ Failed       : {}", errorCount.get());
        log.info("   ⏱ Duration     : {} ms", testDurationMs);
        log.info("   📈 Throughput  : {} orders/sec",
                String.format("%.2f", throughput));
        log.info("   📉 Average RTT : {} ms",
                String.format("%.2f", avgRtt));

        log.info("\n📊 Latency Distribution (ms)");
        channel.shutdownNow();
    }
}
