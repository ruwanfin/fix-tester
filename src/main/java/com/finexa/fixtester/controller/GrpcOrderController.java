package com.finexa.fixtester.controller;

import com.finexa.fixtester.dto.PlaceOrderRequest;
import com.finexa.fixtester.dto.PlaceOrderResponse;
import com.finexa.fixtester.service.GrpcOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grpc")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GrpcOrderController {

    private final GrpcOrderService grpcOrderService;

    @PostMapping("/place-order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("Received place-order request: symbol={}, side={}, qty={}, price={}, target={}:{}",
                request.getSymbol(), request.getSide(), request.getQuantity(),
                request.getPrice(), request.getGrpcHost(), request.getGrpcPort());
        return ResponseEntity.ok(grpcOrderService.placeOrder(request));
    }
}
