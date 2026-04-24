package com.finexa.fixtester.service;

import com.finexa.fixtester.dto.ExecutionReportRequest;
import com.finexa.fixtester.dto.FixDmaRequest;
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

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FIXMessageService  {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${exchange.message.topic:from-exchange}")
    private String exchangeTopic;

    public SendMessageResponse sendExecutionReport(ExecutionReportRequest request) {
        try {
            String kafkaMessage = buildKafkaMessage(request);
            log.info("Sending FIX message to topic {}: {}", exchangeTopic, formatForLog(kafkaMessage));

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(buildRecord(exchangeTopic, request.getClOrdId(), kafkaMessage));
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

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(buildRecord(exchangeTopic, null, rawMessage));
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
        String lastClOrdId = request.getClOrdId();

        for (int i = 0; i < request.getSteps().size(); i++) {
            ScenarioStep step = request.getSteps().get(i);
            String effectiveClOrdId = step.getClOrdId() != null && !step.getClOrdId().isEmpty()
                    ? step.getClOrdId() : lastClOrdId;

            ExecutionReportRequest execRequest = new ExecutionReportRequest();
            execRequest.setSessionId(request.getSessionId());
            execRequest.setSenderCompId(request.getSenderCompId());
            execRequest.setTargetCompId(request.getTargetCompId());
            execRequest.setAccount(request.getAccount());
            execRequest.setClOrdId(effectiveClOrdId);
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
            execRequest.setOrigClOrdId(step.getOrigClOrdId() != null && !step.getOrigClOrdId().isEmpty()
                    ? step.getOrigClOrdId()
                    : ("REPLACED".equalsIgnoreCase(step.getExecType()) || "CANCELED".equalsIgnoreCase(step.getExecType())
                        ? lastClOrdId : null));
            execRequest.setExecId(step.getExecId());
            execRequest.setMsgSeqNum(step.getMsgSeqNum() > 0 ? step.getMsgSeqNum() : autoSeqNum++);

            SendMessageResponse response = sendExecutionReport(execRequest);
            if (effectiveClOrdId != null && !effectiveClOrdId.isEmpty()) {
                lastClOrdId = effectiveClOrdId;
            }

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
            case "REPLACED" -> FIXMessageBuilder.createReplaced(
                    request.getClOrdId(),
                    request.getOrigClOrdId() != null ? request.getOrigClOrdId() : request.getClOrdId(),
                    request.getOrderId(),
                    request.getSymbol(),
                    side,
                    request.getQuantity(),
                    request.getPrice(),
                    request.getCumQty(),
                    request.getLeavesQty() > 0 ? request.getLeavesQty() : request.getQuantity(),
                    request.getAvgPrice(),
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

    public SendMessageResponse sendFixDmaOrder(FixDmaRequest request) {
        try {
            String kafkaMessage = buildDmaKafkaMessage(request);
            String key = request.getClOrdId();
            log.info("Sending FIXDMA ({}) to topic {}: {}", request.getMsgType(), exchangeTopic, formatForLog(kafkaMessage));

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(buildRecord(exchangeTopic, key, kafkaMessage));
            SendResult<String, String> result = future.get(10, TimeUnit.SECONDS);

            log.info("FIXDMA message sent. Topic: {}, Partition: {}, Offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return new SendMessageResponse(
                    true,
                    "FIXDMA message sent successfully",
                    formatForLog(kafkaMessage),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition()
            );
        } catch (Exception e) {
            log.error("Failed to send FIXDMA message: {}", e.getMessage(), e);
            return new SendMessageResponse(false, "Failed to send message: " + e.getMessage(),
                    null, exchangeTopic, null, null);
        }
    }

    private String buildDmaKafkaMessage(FixDmaRequest r) {
        String session    = r.getSessionId()    != null && !r.getSessionId().isEmpty()    ? r.getSessionId()    : "BBG_SESSION";
        String sender     = r.getSenderCompId() != null && !r.getSenderCompId().isEmpty() ? r.getSenderCompId() : "BBG";
        String target     = r.getTargetCompId() != null && !r.getTargetCompId().isEmpty() ? r.getTargetCompId() : "FINXAFC";
        String subId      = r.getSenderSubId()  != null ? r.getSenderSubId()  : "null";
        String exchange   = r.getExchange()     != null ? r.getExchange()     : "TDWL";
        String tif        = r.getTif()          != null ? r.getTif()          : "0";
        String ordType    = r.getOrdType()      != null ? r.getOrdType()      : "2";
        String secIdSrc   = r.getSecurityIdSource() != null ? r.getSecurityIdSource() : "8";
        String secId      = r.getSecurityId()   != null && !r.getSecurityId().isEmpty() ? r.getSecurityId() : r.getSymbol();
        String handlInst  = r.getHandlInst()    != null ? r.getHandlInst()    : "1";

        return switch (r.getMsgType().toUpperCase()) {
            case "D" -> FIXMessageBuilder.createDmaNewOrder(
                    session, r.getMsgSeqNum(), sender, target, subId,
                    handlInst, r.getClOrdId(), r.getAccount(), r.getOrigClOrdId(),
                    r.getSymbol(), secId, secIdSrc,
                    r.getSide(), r.getPrice(), (int) r.getQuantity(),
                    ordType, exchange, tif);
            case "G" -> FIXMessageBuilder.createDmaAmend(
                    session, r.getMsgSeqNum(), sender, target, subId,
                    r.getClOrdId(), r.getAccount(), r.getSymbol(),
                    r.getSide(), r.getOrigClOrdId(),
                    r.getPrice(), (int) r.getMinQty(), (int) r.getQuantity(),
                    ordType, exchange, tif);
            case "F" -> FIXMessageBuilder.createDmaCancel(
                    session, r.getMsgSeqNum(), sender, target, subId,
                    r.getClOrdId(), r.getAccount(), r.getSymbol(),
                    r.getSide(), (int) r.getQuantity(), r.getOrigClOrdId());
            default -> throw new IllegalArgumentException("Unknown FIXDMA message type: " + r.getMsgType());
        };
    }

    private ProducerRecord<String, String> buildRecord(String topic, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        record.headers().add(new RecordHeader("tn", "DEFAULT_TENANT".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("et", "23041".getBytes(StandardCharsets.UTF_8)));
        return record;
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
