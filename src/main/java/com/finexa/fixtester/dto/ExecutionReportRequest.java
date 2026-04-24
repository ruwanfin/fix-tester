package com.finexa.fixtester.dto;

import lombok.Data;

@Data
public class ExecutionReportRequest {
    // Session/Connection fields
    private String sessionId = "SESSION001";

    // FIX Header fields
    private int msgSeqNum;           // Tag 34 - Message Sequence Number
    private String senderCompId;     // Tag 49 - Sender Comp ID (e.g., XSAU, NASDAQ)
    private String targetCompId;     // Tag 56 - Target Comp ID (e.g., FINXAMC, OMS)

    // Order identification fields
    private String clOrdId;          // Tag 11 - Client Order ID
    private String orderId;          // Tag 37 - Exchange Order ID
    private String execId;           // Tag 17 - Execution ID
    private String account;          // Tag 1  - Account (e.g., 08012345678)

    // Instrument fields
    private String symbol;           // Tag 55 - Symbol

    // Order details
    private String side;             // Tag 54 - Side: BUY (1) or SELL (2)
    private double quantity;         // Tag 38 - Order Quantity
    private double price;            // Tag 44 - Price
    private String exchange = "XSAU"; // Tag 207 - Exchange (also used for SenderCompId default)
    private String ordType = "2";    // Tag 40 - Order Type: 1=Market, 2=Limit

    // Execution fields
    private String execType;         // Tag 150 - Exec Type: NEW, PARTIAL_FILL, FILL, REPLACED, CANCELED, REJECTED, PENDING_NEW
    private double fillQty;          // Tag 32 - Last Quantity (fill qty)
    private double fillPrice;        // Tag 31 - Last Price (fill price)
    private double cumQty;           // Tag 14 - Cumulative Quantity
    private double leavesQty;        // Tag 151 - Leaves Quantity
    private double avgPrice;         // Tag 6  - Average Price

    // Cancel/Replace fields
    private String origClOrdId;      // Tag 41 - Original Client Order ID

    // Reject fields
    private String rejectReason;     // Tag 58 - Text (reject reason)
    private int rejectCode;          // Tag 103 - Order Reject Reason code
}
