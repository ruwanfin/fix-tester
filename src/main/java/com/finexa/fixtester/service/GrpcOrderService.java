package com.finexa.fixtester.service;

import com.finexa.fixtester.dto
        .PlaceOrderRequest;
import com.finexa.fixtester.dto.PlaceOrderResponse;
import com.finexa.orderManage.core.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GrpcOrderService {

    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {

        String target = request.getGrpcHost() + ":" + request.getGrpcPort();

        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(target)
                .usePlaintext()
                .build();

        try {
            OrderServiceGrpc.OrderServiceBlockingStub stub =
                    OrderServiceGrpc.newBlockingStub(channel);

            // Auto-generate IDs if not provided
            String unqReqId = notBlank(request.getUnqReqId())
                    ? request.getUnqReqId()
                    : "REQ-" + System.currentTimeMillis();

            String sessionId = notBlank(request.getSessionId())
                    ? request.getSessionId()
                    : "S-" + System.currentTimeMillis();

            String clOrdId = notBlank(request.getClOrdId())
                    ? request.getClOrdId()
                    : "CLORD-" + System.currentTimeMillis();

            String tradeDate = notBlank(request.getTradeDate())
                    ? request.getTradeDate()
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Build header
            FineXaMsgHeaderGRPC header = FineXaMsgHeaderGRPC.newBuilder()
                    .setProduct(request.getProduct())
                    .setTenantCode(request.getTenantCode())
                    .setLoginId(request.getLoginId())
                    .setClientIp(request.getClientIp())
                    .setSessionId(sessionId)
                    .setComponentId(request.getComponentId())
                    .setUnqReqId(unqReqId)
                    .build();

            // Build order body
            OrderRequestBodyGRPC body = OrderRequestBodyGRPC.newBuilder()
                    .setSymbol(request.getSymbol())
                    .setPrice(Double.parseDouble(request.getPrice()))
                    .setTradingAccountID(request.getTradingAccountID())
                    .setExchange(request.getExchange())
                    .setType(request.getType())
                    .setSide(request.getSide())
                    .setQuantity(Long.parseLong(request.getQuantity()))
                    .setTif(request.getTif())
                    .setTradeDate(tradeDate)
                    .setCustomerNo(request.getCustomerNo())
                    .setClOrdId(clOrdId)
                    .setOrderMode(request.getOrderMode())
                    .setOrdCat(request.getOrdCat())
                    .setBypassRms(request.getBypassRms())
                    .setRemark(request.getRemark() != null ? request.getRemark() : "")
                    .setExecBrokerID(request.getExecBrokerID())
                    .setCustodianID(request.getCustodianID())
                    .build();

            // Build request wrapper
            OrderRequestGRPC grpcRequest = OrderRequestGRPC.newBuilder()
                    .setHeader(header)
                    .setServiceType(request.getServiceType())
                    .setTimestamp(System.currentTimeMillis())
                    .setData(body)
                    .build();

            log.info("Sending gRPC order to {}: unqReqId={}, symbol={}, side={}, qty={}, price={}",
                    target, unqReqId, request.getSymbol(), request.getSide(),
                    request.getQuantity(), request.getPrice());

            long rttStart = System.nanoTime();

            OrderResponseGRPC response = stub
                    .withDeadlineAfter(60, TimeUnit.SECONDS)
                    .processOrder(grpcRequest);

            long rttMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - rttStart);

            log.info("gRPC order response: statusCode={}, status={}, desc={}, rttMs={}",
                    response.getStatusCode(),
                    response.getData().getStatus(),
                    response.getData().getStatusDescription(),
                    rttMs);

            return new PlaceOrderResponse(
                    true,
                    "Order sent successfully",
                    response.getStatusCode(),
                    response.getData().getStatus(),
                    response.getData().getReference(),
                    response.getData().getStatusDescription(),
                    response.getData().getErrorCode(),
                    response.getData().getErrorMessage(),
                    response.getData().getErrorDetail(),
                    rttMs
            );

        } catch (StatusRuntimeException e) {
            log.error("gRPC StatusRuntimeException: {}", e.getStatus(), e);
            return new PlaceOrderResponse(
                    false, "gRPC Error: " + e.getStatus(),
                    0, 0, null, null, 0, e.getMessage(), null, 0L);

        } catch (Exception e) {
            log.error("Unexpected gRPC error: {}", e.getMessage(), e);
            return new PlaceOrderResponse(
                    false, "Error: " + e.getMessage(),
                    0, 0, null, null, 0, e.getMessage(), null, 0L);

        } finally {
            channel.shutdownNow();
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
