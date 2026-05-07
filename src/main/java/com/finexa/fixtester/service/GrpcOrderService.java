package com.finexa.fixtester.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finexa.fixtester.dto
        .PlaceOrderRequest;
import com.finexa.fixtester.dto.PlaceOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GrpcOrderService {

    private static final int DEFAULT_WS_PORT = 8085;
    private static final String DEFAULT_WS_PATH = "/oms-streaming-api";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {

        URI target = URI.create("ws://" + request.getGrpcHost() + ":"
                + resolvePort(request.getGrpcPort()) + resolvePath(request.getWebsocketPath()));

        try {
            if (requiresExistingClOrdId(request.getServiceType()) && !notBlank(request.getClOrdId())) {
                return new PlaceOrderResponse(
                        false,
                        serviceName(request.getServiceType()) + " requires Cl Ord ID",
                        0,
                        0,
                        null,
                        null,
                        0,
                        "Cl Ord ID is required for amend/cancel",
                        null,
                        0L);
            }

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

            String wsRequest = buildWebSocketRequestJson(request, unqReqId, sessionId, clOrdId, tradeDate);

            log.info("Sending WebSocket {} to {}: serviceType={}, unqReqId={}, clOrdId={}, symbol={}, side={}, qty={}, price={}",
                    serviceName(request.getServiceType()), target, request.getServiceType(), unqReqId, clOrdId,
                    request.getSymbol(), request.getSide(),
                    request.getQuantity(), request.getPrice());

            long rttStart = System.nanoTime();

            SingleMessageListener listener = new SingleMessageListener();
            WebSocket webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(target, listener)
                    .get(10, TimeUnit.SECONDS);

            webSocket.sendText(wsRequest, true).get(10, TimeUnit.SECONDS);
            String responseJson = listener.response().get(60, TimeUnit.SECONDS);
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done");

            long rttMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - rttStart);
            JsonNode response = objectMapper.readTree(responseJson);
            JsonNode data = response.path("d");

            int statusCode = intValue(response.path("st"), 0);
            int status = intValue(data.path("sts"), 0);
            String statusDescription = textValue(data.path("stsDesc"));
            String errorMessage = textValue(data.path("em"));
            boolean success = errorMessage == null || errorMessage.isBlank();

            log.info("WebSocket order response: statusCode={}, status={}, desc={}, rttMs={}",
                    statusCode,
                    status,
                    statusDescription,
                    rttMs);

            return new PlaceOrderResponse(
                    success,
                    success ? serviceName(request.getServiceType()) + " sent successfully" : serviceName(request.getServiceType()) + " rejected",
                    statusCode,
                    status,
                    firstText(data.path("ref"), data.path("reference"), data.path("clOrdId")),
                    statusDescription,
                    intValue(data.path("ec"), 0),
                    errorMessage,
                    textValue(data.path("ed")),
                    rttMs
            );

        } catch (Exception e) {
            log.error("Unexpected WebSocket order error: {}", e.getMessage(), e);
            return new PlaceOrderResponse(
                    false, "Error: " + e.getMessage(),
                    0, 0, null, null, 0, e.getMessage(), null, 0L);
        }
    }

    private String buildWebSocketRequestJson(PlaceOrderRequest request, String unqReqId,
                                             String sessionId, String clOrdId, String tradeDate) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("s", request.getServiceType());
        root.put("tm", System.currentTimeMillis());
        root.put("st", 0);

        ObjectNode header = root.putObject("h");
        header.put("p", request.getProduct());
        header.put("tn", request.getTenantCode());
        header.put("l", request.getLoginId());
        header.put("ip", request.getClientIp());
        header.put("sd", sessionId);
        header.put("cId", request.getComponentId());
        header.put("u", unqReqId);
        header.put("o", request.getOrgId());

        ObjectNode body = root.putObject("d");
        body.put("symbol", request.getSymbol());
        body.put("price", request.getPrice());
        body.put("tradingAccId", request.getTradingAccountID());
        body.put("tradingAccNo", request.getTradingAccountNo());
        body.put("exchange", request.getExchange());
        body.put("type", request.getType());
        body.put("side", request.getSide());
        body.put("quantity", request.getQuantity());
        body.put("tif", request.getTif());
        body.put("tifDt", tradeDate);
        body.put("custNo", request.getCustomerNo());
        body.put("clOrdId", clOrdId);
        body.put("ordMode", request.getOrderMode());
        body.put("ordCat", request.getOrdCat());
        body.put("rmk", request.getRemark() != null ? request.getRemark() : "");
        body.put("execBrokerId", request.getExecBrokerID());
        body.put("custodyInstId", request.getCustodianID());
        body.put("currencyCode", request.getCurrencyCode());
        body.put("minPrice", request.getMinPrice());
        body.put("maxPrice", request.getMaxPrice());
        body.put("lstTrdPrice", request.getLstTrdPrice());
        body.put("prvClsPrice", request.getPrvClsPrice());
        body.put("todayClsPrice", request.getTodayClsPrice());
        body.put("instruType", request.getInstruType());
        body.put("byPassRMS", request.getBypassRms());
        body.put("dClsQty", request.getDClsQty());
        body.put("mFillQty", request.getMFillQty());

        return objectMapper.writeValueAsString(root);
    }

    private int resolvePort(int port) {
        return port > 0 ? port : DEFAULT_WS_PORT;
    }

    private String resolvePath(String path) {
        String resolved = notBlank(path) ? path : DEFAULT_WS_PATH;
        return resolved.startsWith("/") ? resolved : "/" + resolved;
    }

    private int intValue(JsonNode node, int fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return fallback;
        }
        if (node.isInt() || node.isLong()) {
            return node.asInt();
        }
        String value = node.asText();
        if (!notBlank(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String textValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return notBlank(value) ? value : null;
    }

    private String firstText(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            String value = textValue(node);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private boolean requiresExistingClOrdId(int serviceType) {
        return serviceType == 3 || serviceType == 4;
    }

    private String serviceName(int serviceType) {
        return switch (serviceType) {
            case 2 -> "new order";
            case 3 -> "cancel";
            case 4 -> "amend";
            default -> "order";
        };
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static class SingleMessageListener implements WebSocket.Listener {
        private final CompletableFuture<String> response = new CompletableFuture<>();
        private final StringBuilder message = new StringBuilder();

        CompletableFuture<String> response() {
            return response;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            message.append(data);
            if (last) {
                response.complete(message.toString());
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            response.completeExceptionally(error);
        }
    }
}
