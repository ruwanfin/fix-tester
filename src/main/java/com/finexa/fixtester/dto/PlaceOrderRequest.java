package com.finexa.fixtester.dto;

import lombok.Data;

@Data
public class PlaceOrderRequest {

    private String grpcHost = "localhost";
    private int    grpcPort = 8085;
    private String websocketPath = "/oms-streaming-api";

    private int    product     = 1;
    private String tenantCode  = "DEFAULT_TENANT";
    private int    loginId     = 6;
    private int    orgId       = 1;
    private String clientIp    = "127.0.0.1";
    private String sessionId;
    private String componentId = "ORDER-TESTER";
    private String unqReqId;

    private int serviceType = 2;

    private String symbol           = "1010";
    private double price            = 20.0;
    private int    tradingAccountID = 20;
    private String tradingAccountNo = "";
    private String exchange         = "TDWL";
    private String type             = "2";
    private int    side             = 1;
    private double quantity         = 1001;
    private int    tif              = 0;
    private String tradeDate;
    private String customerNo       = "CUST-001";
    private String clOrdId;
    private int    orderMode        = 0;
    private int    ordCat           = 1;
    private int    bypassRms        = 0;
    private String remark           = "test";
    private int    execBrokerID     = 74;
    private int    custodianID      = 74;
    private String currencyCode     = "";
    private double minPrice         = 0;
    private double maxPrice         = 0;
    private double lstTrdPrice      = 0;
    private double prvClsPrice      = 0;
    private double todayClsPrice    = 0;
    private int    instruType       = 0;
    private double dClsQty          = 0;
    private double mFillQty         = 0;
}
