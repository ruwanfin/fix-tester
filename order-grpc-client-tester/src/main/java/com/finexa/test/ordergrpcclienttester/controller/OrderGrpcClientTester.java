//package com.finexa.test.ordergrpcclienttester.controller;
//
//import com.finexa.orderManage.core.grpc.*;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import io.grpc.StatusRuntimeException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Slf4j
//@SpringBootApplication
//public class OrderGrpcClientTester implements CommandLineRunner {
//
//    public static void main(String[] args) {
//        SpringApplication.run(OrderGrpcClientTester.class, args);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        // -------------------------------------------------
//        // ARGUMENT VALIDATION
//        // -------------------------------------------------
//        if (args.length < 2) {
//            System.err.println(
//                    "Usage: java -jar order-grpc-client-tester.jar <concurrency> <totalOrders>"
//            );
//            System.exit(1);
//        }
//
//        int concurrency = Integer.parseInt(args[0]);
//        int totalOrders = Integer.parseInt(args[1]);
//
//        String target = "192.168.122.163:30094";
//        // String target = "localhost:9092";
//
//        log.info("======================================");
//        log.info(" gRPC Load Test Configuration");
//        log.info(" Target       : {}", target);
//        log.info(" Concurrency  : {}", concurrency);
//        log.info(" Total Orders : {}", totalOrders);
//        log.info("======================================");
//
//        // -------------------------------------------------
//        // gRPC SETUP
//        // -------------------------------------------------
//        ManagedChannel channel = ManagedChannelBuilder
//                .forTarget(target)
//                .usePlaintext()
//                .build();
//
//        OrderServiceGrpc.OrderServiceBlockingStub stub =
//                OrderServiceGrpc.newBlockingStub(channel);
//
//        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
//
//        // -------------------------------------------------
//        // METRICS
//        // -------------------------------------------------
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger errorCount = new AtomicInteger(0);
//        AtomicLong totalRttMs = new AtomicLong(0);
//        ConcurrentMap<Long, AtomicInteger> latencyBuckets = new ConcurrentHashMap<>();
//
//        long testStart = System.currentTimeMillis();
//
//        log.info("🚀 Starting gRPC load test");
//
//        List<Future<?>> futures = new ArrayList<>();
//
//        // -------------------------------------------------
//        // SUBMIT TASKS
//        // -------------------------------------------------
//        for (int i = 1; i <= totalOrders; i++) {
//            int index = i;
//
//            futures.add(executor.submit(() -> {
//
//                FineXaMsgHeaderGRPC header = FineXaMsgHeaderGRPC.newBuilder()
//                        .setProduct(1)
//                        .setTenantCode("DEFAULT_TENANT")
//                        .setLoginId(1001)
//                        .setClientIp("127.0.0.1")
//                        .setSessionId("S-" + index)
//                        .setComponentId("ORDER-TESTER")
//                        .setUnqReqId("REQ-" + index)
//                        .build();
//
//                OrderRequestBodyGRPC body = OrderRequestBodyGRPC.newBuilder()
//                        .setSymbol("1010")
//                        .setQuantity(100 + index)
//                        .setPrice(150.50)
//                        .setSide(1)
//                        .setTradingAccountID(23)
//                        .setCashAcntID(32)
//                        .setOrdCat(1)
//                        .setExchange("TDWL")
//                        .setMarketCode("NORMAL")
//                        .build();
//
//                OrderRequestGRPC request = OrderRequestGRPC.newBuilder()
//                        .setHeader(header)
//                        .setServiceType(2)
//                        .setTimestamp(System.currentTimeMillis())
//                        .setData(body)
//                        .build();
//
//                try {
//                    long rttStartNs = System.nanoTime();
//
//                    OrderResponseGRPC response = stub
//                            .withDeadlineAfter(60, TimeUnit.SECONDS)
//                            .processOrder(request);
//
//                    long rttMs = TimeUnit.NANOSECONDS.toMillis(
//                            System.nanoTime() - rttStartNs
//                    );
//
//                    successCount.incrementAndGet();
//                    totalRttMs.addAndGet(rttMs);
//
//                    latencyBuckets
//                            .computeIfAbsent(rttMs, k -> new AtomicInteger())
//                            .incrementAndGet();
//
//                    log.info("✅ [{}] OK → {} ms | Status={}",
//                            request.getHeader().getUnqReqId(),
//                            rttMs,
//                            response.getData().getStatus());
//
//                } catch (StatusRuntimeException e) {
//                    errorCount.incrementAndGet();
//                    log.error("❌ [{}] gRPC Error: {}",
//                            request.getHeader().getUnqReqId(),
//                            e.getStatus());
//                } catch (Exception e) {
//                    errorCount.incrementAndGet();
//                    log.error("❌ [{}] Unexpected Error: {}",
//                            request.getHeader().getUnqReqId(),
//                            e.getMessage(), e);
//                }
//            }));
//        }
//
//        // -------------------------------------------------
//        // WAIT FOR COMPLETION
//        // -------------------------------------------------
//        for (Future<?> f : futures) {
//            try {
//                f.get(60, TimeUnit.SECONDS);
//            } catch (TimeoutException e) {
//                log.warn("⚠️ Worker timeout");
//            }
//        }
//
//        executor.shutdown();
//        executor.awaitTermination(60, TimeUnit.SECONDS);
//
//        long testDurationMs = System.currentTimeMillis() - testStart;
//
//        double throughput =
//                successCount.get() == 0
//                        ? 0
//                        : (successCount.get() * 1000.0) / testDurationMs;
//
//        double avgRtt =
//                successCount.get() == 0
//                        ? 0
//                        : (double) totalRttMs.get() / successCount.get();
//
//        // -------------------------------------------------
//        // FINAL REPORT
//        // -------------------------------------------------
//        log.info("\n📊 Latency Distribution (ms)");
//        latencyBuckets.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .forEach(e ->
//                        log.info("   {} ms -> {} requests",
//                                e.getKey(), e.getValue().get())
//                );
//
//        log.info("\n🏁 Load Test Complete");
//        log.info("   ✅ Success      : {}", successCount.get());
//        log.info("   ❌ Failed       : {}", errorCount.get());
//        log.info("   ⏱ Duration     : {} ms", testDurationMs);
//        log.info("   📈 Throughput  : {} orders/sec",
//                String.format("%.2f", throughput));
//        log.info("   📉 Average RTT : {} ms",
//                String.format("%.2f", avgRtt));
//
//        // -------------------------------------------------
//        // PLAIN CONSOLE SUMMARY (USEFUL ON LINUX)
//        // -------------------------------------------------
//        System.out.println("\n====== FINAL SUMMARY ======");
//        System.out.println("Success      : " + successCount.get());
//        System.out.println("Failed       : " + errorCount.get());
//        System.out.println("Duration(ms) : " + testDurationMs);
//        System.out.println("Throughput  : " + String.format("%.2f", throughput));
//        System.out.println("Avg RTT(ms) : " + String.format("%.2f", avgRtt));
//
//        channel.shutdownNow();
//        System.exit(0);
//    }
//}
