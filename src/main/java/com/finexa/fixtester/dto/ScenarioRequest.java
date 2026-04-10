package com.finexa.fixtester.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScenarioRequest {
    private String sessionId = "SESSION001";
    private String senderCompId = "XSAU";
    private String targetCompId = "FINXAFC";
    private String account = "CSD10000";
    private String clOrdId;
    private String orderId;
    private String symbol = "1010";
    private String side = "BUY";
    private double quantity = 1001;
    private double price = 20.0;
    private String exchange = "XSAU";
    private String ordType = "2";
    private List<ScenarioStep> steps;
}
