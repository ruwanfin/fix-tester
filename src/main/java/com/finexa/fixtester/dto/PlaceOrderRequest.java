package com.finexa.fixtester.dto;

import lombok.Data;

@Data
public class PlaceOrderRequest {

    // ── Connection ────────────────────────────────────────────
    private String grpcHost = "localhost";
    private int    grpcPort = 9095;

    // ── Message Header (FineXaMsgHeaderGRPC) ─────────────────
    private int    product     = 1;
    private String tenantCode  = "DEFAULT_TENANT";
    private int    loginId     = 6;
    private int   orgId = 1;
    private String clientIp    = "127.0.0.1";
    private String sessionId;       // auto-generated if blank
    private String componentId = "ORDER-TESTER";
    private String unqReqId;        // auto-generated if blank

    // ── Service envelope ─────────────────────────────────────
    private int serviceType = 2;

    // ── Order Body (OrderRequestBodyGRPC) ─────────────────────
    private String symbol           = "1010";
    private String price            = "20.0";
    private int    tradingAccountID = 20;
    private String exchange         = "TDWL";
    private String type             = "2";      // 1=Market, 2=Limit
    private int    side             = 1;        // 1=Buy, 2=Sell
    private String   quantity       = "1001";
    private int    tif              = 0;        // 0=Day, 1=GTC, 3=IOC, 4=FOK
    private String tradeDate;                  // auto-set to today if blank
    private String customerNo       = "CUST-001";
    private String clOrdId;                    // auto-generated if blank
    private int    orderMode        = 0;
    private int    ordCat           = 1;
    private int    bypassRms        = 0;
    private String remark           = "test";
    private int    execBrokerID     = 1;
    private int    custodianID      = 1;
}
