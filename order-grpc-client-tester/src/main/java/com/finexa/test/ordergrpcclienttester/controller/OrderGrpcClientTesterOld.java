//package com.finexa.test.ordergrpcclienttester;
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
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Slf4j
//@SpringBootApplication
//public class OrderGrpcClientTesterOld implements CommandLineRunner {
//
//    public static void main(String[] args) {
//        SpringApplication.run(OrderGrpcClientTesterOld.class, args);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        // target = "localhost:9092";
//        String target = "localhost:9095";
//        //String target = "192.168.122.163:30094";
//
//        int totalOrders = 100;//1000Ts
//        int concurrency = 1;//20
//
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
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger errorCount = new AtomicInteger(0);
//
//        long start = System.currentTimeMillis();
//        log.info("🚀 Starting gRPC load test ({} orders, concurrency={})", totalOrders, concurrency);
//
//        List<Future<?>> futures = new ArrayList<>();
//
//        for (int i = 1; i <= totalOrders; i++) {
//            int index = i;
//
//            futures.add(executor.submit(() -> {
//
//                // -------------------------
//                // HEADER
//                // -------------------------
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
//                // -------------------------
//                // REQUEST BODY
//                // -------------------------
//                OrderRequestBodyGRPC body = OrderRequestBodyGRPC.newBuilder()
//                        .setSymbol("1010")
//                        .setQuantity(100 + index)
//                        .setPrice(150.50)
//                        .setSide(1) // index % 2 == 0 ? 1 : 2
//                        .setTradingAccountID(23)
//                        .setTradingAccountID(23)
//                        .setCashAcntID(32)
//                        .setOrdCat(1)
//                        .setExchange("TDWL")
//                        .setMarketCode("NORMAL")
//                        .build();
//
//                // -------------------------
//                // WRAPPER
//                // -------------------------
//                OrderRequestGRPC request = OrderRequestGRPC.newBuilder()
//                        .setHeader(header)
//                        .setServiceType(2)
//                        .setTimestamp(System.currentTimeMillis())
//                        .setData(body)
//                        .build();
//
//                try {
//                    log.info("Sending order start [{}]", request.getHeader().getUnqReqId());
//                    OrderResponseGRPC response = stub
//                            .withDeadlineAfter(60, TimeUnit.SECONDS)
//                            .processOrder(request);
//                    log.info("Sending order end [{}]", request.getHeader().getUnqReqId());
//
//                    successCount.incrementAndGet();
//                    log.info("✅ [{}] OK → Status: {}, Desc: {}",
//                            request.getHeader().getUnqReqId(),
//                            response.getData().getStatus(),
//                            response.getData().getStatusDescription());
//
//                } catch (StatusRuntimeException e) {
//                    errorCount.incrementAndGet();
//                    log.error("❌ [{}] gRPC Error: {}",
//                            request.getData().getClOrdId(),
//                            e.getStatus(), e);
//
//                } catch (Exception e) {
//                    errorCount.incrementAndGet();
//                    log.error("❌ [{}] Unexpected Error: {}",
//                            request.getData().getClOrdId(),
//                            e.getMessage(), e);
//                }
//            }));
//        }
//
//        // Wait for all tasks
//        for (Future<?> f : futures) {
//            try {
//                f.get(10, TimeUnit.SECONDS);
//            } catch (TimeoutException e) {
//                log.warn("⚠️ Timeout waiting for worker");
//            } catch (Exception e) {
//                log.warn("⚠️ Worker execution failed: {}", e.getMessage());
//            }
//        }
//
//        executor.shutdown();
//        executor.awaitTermination(60, TimeUnit.SECONDS);
//
//        long duration = System.currentTimeMillis() - start;
//        double tps = (successCount.get() * 1000.0) / duration;
//
//        log.info("\n🏁 Load Test Complete");
//        log.info("   ✅ Success : {}", successCount.get());
//        log.info("   ❌ Failed  : {}", errorCount.get());
//        log.info("   ⏱ Duration: {} ms", duration);
//        log.info("   📈 Throughput: {} orders/sec", tps);
//
//        channel.shutdownNow();
//    }
//}
