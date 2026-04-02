package com.finexa.fixtester.service;

import com.finexa.fixtester.dto.ExecutionReportRequest;
import com.finexa.fixtester.dto.ScenarioRequest;
import com.finexa.fixtester.dto.ScenarioResponse;
import com.finexa.fixtester.dto.ScenarioStep;
import com.finexa.fixtester.dto.SendMessageResponse;
import com.finexa.fixtester.util.FIXMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FIXMessageService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${exchange.message.topic:from-exchange}")
    private String exchangeTopic;

    public SendMessageResponse sendExecutionReport(ExecutionReportRequest request) {
        try {
            String kafkaMessage = buildKafkaMessage(request);
            log.info("Sending FIX message to topic {}: {}", exchangeTopic, formatForLog(kafkaMessage));

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(exchangeTopic, request.getClOrdId(), kafkaMessage);
            SendResult<String, String> result = future.get(10, TimeUnit.SECONDS);

            log.info("Message sent successfully. Topic: {}, Partition: {}, Offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return new SendMessageResponse(
                    true,
                    "Message sent successfully",
                    formatForLog(kafkaMessage),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition()
            );
        } catch (Exception e) {
            log.error("Failed to send FIX message: {}", e.getMessage(), e);
            return new SendMessageResponse(
                    false,
                    "Failed to send message: " + e.getMessage(),
                    null,
                    exchangeTopic,
                    null,
                    null
            );
        }
    }

    public SendMessageResponse sendRawMessage(String rawMessage) {
        try {
            log.info("Sending raw message to topic {}: {}", exchangeTopic, formatForLog(rawMessage));

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(exchangeTopic, rawMessage);
            SendResult<String, String> result = future.get(10, TimeUnit.SECONDS);

            log.info("Raw message sent successfully. Topic: {}, Partition: {}, Offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return new SendMessageResponse(
                    true,
                    "Raw message sent successfully",
                    formatForLog(rawMessage),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition()
            );
        } catch (Exception e) {
            log.error("Failed to send raw message: {}", e.getMessage(), e);
            return new SendMessageResponse(
                    false,
                    "Failed to send message: " + e.getMessage(),
                    null,
                    exchangeTopic,
                    null,
                    null
            );
        }
    }

    public ScenarioResponse sendScenario(ScenarioRequest request) {
        List<ScenarioResponse.StepResult> stepResults = new ArrayList<>();
        boolean allSuccess = true;
        int autoSeqNum = 1;

        for (int i = 0; i < request.getSteps().size(); i++) {
            ScenarioStep step = request.getSteps().get(i);

            ExecutionReportRequest execRequest = new ExecutionReportRequest();
            execRequest.setSessionId(request.getSessionId());
            execRequest.setSenderCompId(request.getSenderCompId());
            execRequest.setTargetCompId(request.getTargetCompId());
            execRequest.setAccount(request.getAccount());
            execRequest.setClOrdId(step.getClOrdId() != null && !step.getClOrdId().isEmpty()
                    ? step.getClOrdId() : request.getClOrdId());
            execRequest.setOrderId(step.getOrderId() != null && !step.getOrderId().isEmpty()
                    ? step.getOrderId() : request.getOrderId());
            execRequest.setSymbol(request.getSymbol());
            execRequest.setSide(request.getSide());
            execRequest.setQuantity(request.getQuantity());
            execRequest.setPrice(request.getPrice());
            execRequest.setExchange(request.getExchange());
            execRequest.setOrdType(request.getOrdType());
            execRequest.setExecType(step.getExecType());
            execRequest.setFillQty(step.getFillQty());
            execRequest.setFillPrice(step.getFillPrice() > 0 ? step.getFillPrice() : request.getPrice());
            execRequest.setCumQty(step.getCumQty());
            execRequest.setLeavesQty(step.getLeavesQty());
            execRequest.setAvgPrice(step.getAvgPrice());
            execRequest.setExecId(step.getExecId());
            execRequest.setMsgSeqNum(step.getMsgSeqNum() > 0 ? step.getMsgSeqNum() : autoSeqNum++);

            SendMessageResponse response = sendExecutionReport(execRequest);

            stepResults.add(new ScenarioResponse.StepResult(
                    i + 1,
                    step.getExecType(),
                    response.isSuccess(),
                    response.getMessage(),
                    response.getRawMessage(),
                    response.getTopic(),
                    response.getOffset(),
                    response.getPartition()
            ));

            if (!response.isSuccess()) {
                allSuccess = false;
            }
        }

        String msg = allSuccess
                ? "All " + request.getSteps().size() + " messages sent successfully"
                : "Some messages failed to send";
        return new ScenarioResponse(allSuccess, msg, stepResults);
    }

    private String buildKafkaMessage(ExecutionReportRequest request) {
        char side = "BUY".equalsIgnoreCase(request.getSide()) ?
                FIXMessageBuilder.SIDE_BUY : FIXMessageBuilder.SIDE_SELL;

        return switch (request.getExecType().toUpperCase()) {
            case "NEW" -> FIXMessageBuilder.createNewOrderAck(
                    request.getClOrdId(),
                    request.getOrderId(),
                    request.getSymbol(),
                    side,
                    request.getQuantity(),
                    request.getPrice(),
                    request.getExchange(),
                    request.getSessionId(),
                    request.getAccount(),
                    request.getMsgSeqNum(),
                    request.getExecId(),
                    request.getSenderCompId(),
                    request.getTargetCompId()
            );
            case "PENDING_NEW" -> FIXMessageBuilder.createPendingNew(
                    request.getClOrdId(),
                    request.getOrderId(),
                    request.getSymbol(),
                    side,
                    request.getQuantity(),
                    request.getPrice(),
                    request.getExchange(),
                    request.getSessionId(),
                    request.getAccount(),
                    request.getMsgSeqNum(),
                    request.getExecId(),
                    request.getSenderCompId(),
                    request.getTargetCompId()
            );
            case "PARTIAL_FILL" -> FIXMessageBuilder.createPartialFill(
                    request.getClOrdId(),
                    request.getOrderId(),
                    request.getSymbol(),
                    side,
                    request.getQuantity(),
                    request.getFillQty(),
                    request.getFillPrice(),
                    request.getCumQty(),
                    request.getLeavesQty(),
                    request.getAvgPrice(),
                    request.getExchange(),
                    request.getSessionId(),
                    request.getAccount(),
                    request.getMsgSeqNum(),
                    request.getExecId(),
                    request.getSenderCompId(),
                    request.getTargetCompId()
            );
            case "FILL" -> FIXMessageBuilder.createFill(
                    request.getClOrdId(),
                    request.getOrderId(),
                    request.getSymbol(),
                    side,
                    request.getQuantity(),                                                          // Tag 38 - order qty
                    request.getFillQty() > 0 ? request.getFillQty() : request.getQuantity(),       // Tag 32 - last qty (this fill)
                    request.getCumQty()  > 0 ? request.getCumQty()  : request.getQuantity(),       // Tag 14 - cumulative qty
                    request.getFillPrice() > 0 ? request.getFillPrice() : request.getPrice(),      // Tag 31 - last px
                    request.getAvgPrice() > 0 ? request.getAvgPrice() : request.getPrice(),        // Tag 6  - avg px
                    request.getExchange(),
                    request.getSessionId(),
                    request.getAccount(),
                    request.getMsgSeqNum(),
                    request.getExecId(),
                    request.getSenderCompId(),
                    request.getTargetCompId()
            );
            case "CANCELED" -> FIXMessageBuilder.createCanceled(
                    request.getClOrdId(),
                    request.getOrigClOrdId() != null ? request.getOrigClOrdId() : request.getClOrdId(),
                    request.getOrderId(),
                    request.getSymbol(),
                    side,
                    request.getQuantity(),
                    request.getCumQty(),
                    request.getLeavesQty(),
                    request.getExchange(),
                    request.getSessionId(),
                    request.getAccount(),
                    request.getMsgSeqNum(),
                    request.getExecId(),
                    request.getSenderCompId(),
                    request.getTargetCompId()
            );
            case "REJECTED" -> FIXMessageBuilder.createReject(
                    request.getClOrdId(),
                    request.getOrderId(),
                    request.getSymbol(),
                    side,
                    request.getQuantity(),
                    request.getRejectReason() != null ? request.getRejectReason() : "Order rejected",
                    request.getRejectCode(),
                    request.getExchange(),
                    request.getSessionId(),
                    request.getAccount(),
                    request.getMsgSeqNum(),
                    request.getExecId(),
                    request.getSenderCompId(),
                    request.getTargetCompId()
            );
            default -> throw new IllegalArgumentException("Unknown execution type: " + request.getExecType());
        };
    }

    /**
     * Format message for logging by replacing non-printable characters.
     */
    private String formatForLog(String message) {
        if (message == null) {
            return null;
        }
        return message
                .replace(String.valueOf(FIXMessageBuilder.SOH), "|")
                .replace(String.valueOf(FIXMessageBuilder.FS), " | ");
    }
}
