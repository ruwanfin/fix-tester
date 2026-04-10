package com.finexa.fixtester.controller;

import com.finexa.fixtester.dto.*;
import com.finexa.fixtester.service.CustomerFileService;
import com.finexa.fixtester.service.GrpcOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grpc")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GrpcOrderController {

    private final GrpcOrderService    grpcOrderService;
    private final CustomerFileService customerFileService;

    @PostMapping("/place-order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("Received place-order request: symbol={}, side={}, qty={}, price={}, target={}:{}",
                request.getSymbol(), request.getSide(), request.getQuantity(),
                request.getPrice(), request.getGrpcHost(), request.getGrpcPort());
        return ResponseEntity.ok(grpcOrderService.placeOrder(request));
    }

    @PostMapping("/bulk-place-order")
    public ResponseEntity<BulkOrderResponse> bulkPlaceOrder(@RequestBody BulkOrderRequest request) {
        int count = request.getOrderCount();
        log.info("Received bulk-place-order request: orderCount={}", count);

        List<CustomerDataEntry> customers = customerFileService.load(count);
        if (customers.isEmpty()) {
            return ResponseEntity.ok(new BulkOrderResponse(0, 0, 0, List.of()));
        }

        int threadPoolSize = Math.min(customers.size(), 50);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        List<CompletableFuture<BulkOrderResult>> futures = new ArrayList<>();
        for (int i = 0; i < customers.size(); i++) {
            final int idx = i + 1;
            final CustomerDataEntry entry = customers.get(i);
            CompletableFuture<BulkOrderResult> future = CompletableFuture.supplyAsync(() -> {
                PlaceOrderRequest req = cloneTemplate(request.getTemplate(), entry);
                PlaceOrderResponse resp = grpcOrderService.placeOrder(req);
                return new BulkOrderResult(idx, entry.getTradeAccId(), entry.getCustomerNo(),
                        request.getTemplate().getSymbol(), resp.isSuccess(), resp.getMessage(), resp.getRttMs());
            }, executor);
            futures.add(future);
        }

        List<BulkOrderResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        executor.shutdown();

        int succeeded = (int) results.stream().filter(BulkOrderResult::isSuccess).count();
        log.info("Bulk order complete: total={}, succeeded={}, failed={}", results.size(), succeeded, results.size() - succeeded);

        return ResponseEntity.ok(new BulkOrderResponse(results.size(), succeeded, results.size() - succeeded, results));
    }

    private PlaceOrderRequest cloneTemplate(PlaceOrderRequest t, CustomerDataEntry entry) {
        PlaceOrderRequest r = new PlaceOrderRequest();
        r.setGrpcHost(t.getGrpcHost());
        r.setGrpcPort(t.getGrpcPort());
        r.setProduct(t.getProduct());
        r.setTenantCode(t.getTenantCode());
        r.setLoginId(t.getLoginId());
        r.setOrgId(t.getOrgId());
        r.setClientIp(t.getClientIp());
        r.setSessionId(null);   // auto-generate per order
        r.setComponentId(t.getComponentId());
        r.setUnqReqId(null);    // auto-generate per order
        r.setServiceType(t.getServiceType());
        r.setExchange(t.getExchange());
        r.setType(t.getType());
        r.setSide(t.getSide());
        r.setPrice(t.getPrice());
        r.setQuantity(t.getQuantity());
        r.setTif(t.getTif());
        r.setTradeDate(t.getTradeDate());
        r.setClOrdId(null);     // auto-generate per order
        r.setOrderMode(t.getOrderMode());
        r.setOrdCat(t.getOrdCat());
        r.setBypassRms(t.getBypassRms());
        r.setRemark(t.getRemark());
        r.setExecBrokerID(t.getExecBrokerID());
        r.setCustodianID(t.getCustodianID());
        // Override from customer file (tradingAccountID + customerNo only; symbol comes from UI)
        r.setTradingAccountID(entry.getTradeAccId());
        r.setCustomerNo(entry.getCustomerNo());
        r.setSymbol(t.getSymbol());
        return r;
    }
}
