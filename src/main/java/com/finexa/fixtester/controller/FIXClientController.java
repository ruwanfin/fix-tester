package com.finexa.fixtester.controller;

import com.finexa.fixtester.dto.ExecutionReportRequest;
import com.finexa.fixtester.dto.FixDmaRequest;
import com.finexa.fixtester.dto.ScenarioRequest;
import com.finexa.fixtester.dto.ScenarioResponse;
import com.finexa.fixtester.dto.SendMessageResponse;
import com.finexa.fixtester.service.FIXMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fix")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FIXClientController {

    private final FIXMessageService fixMessageService;

    @PostMapping("/execution-report")
    public ResponseEntity<SendMessageResponse> sendExecutionReport(@RequestBody ExecutionReportRequest request) {
        log.info("Received execution report request: clOrdId={}, execType={}", request.getClOrdId(), request.getExecType());
        SendMessageResponse response = fixMessageService.sendExecutionReport(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scenario")
    public ResponseEntity<ScenarioResponse> sendScenario(@RequestBody ScenarioRequest request) {
        log.info("Received scenario request with {} steps for clOrdId={}",
                request.getSteps() != null ? request.getSteps().size() : 0, request.getClOrdId());
        ScenarioResponse response = fixMessageService.sendScenario(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/raw")
    public ResponseEntity<SendMessageResponse> sendRawMessage(@RequestBody String rawMessage) {
        log.info("Received raw message request");
        SendMessageResponse response = fixMessageService.sendRawMessage(rawMessage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fixdma")
    public ResponseEntity<SendMessageResponse> sendFixDmaOrder(@RequestBody FixDmaRequest request) {
        log.info("Received FIXDMA request: type={}, clOrdId={}", request.getMsgType(), request.getClOrdId());
        SendMessageResponse response = fixMessageService.sendFixDmaOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FIX Client is running");
    }
}
