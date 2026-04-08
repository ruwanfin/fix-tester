package com.finexa.fixtester.dto;

import lombok.Data;

@Data
public class FixDmaRequest {

    // Kafka wrapper
    private String sessionId;       // Kafka session ID (e.g. "BBG_SESSION")

    // FIX Header
    private String senderCompId;    // Tag 49 (e.g. BBG)
    private String targetCompId;    // Tag 56 (e.g. FINEXA)
    private int msgSeqNum;          // Tag 34
    private String senderSubId;     // Tag 50 (Bloomberg sends literal "null")

    // Message type: D = New Order, G = Amend (Cancel/Replace), F = Cancel
    private String msgType;

    // Common order fields
    private String clOrdId;         // Tag 11 – new order/request ID
    private String origClOrdId;     // Tag 41 – original order ID (required for G and F; optional ref for D)
    private String account;         // Tag 1  – trading account (used in G, F)
    private String symbol;          // Tag 55
    private String securityId;      // Tag 48
    private String securityIdSource; // Tag 22 (default "8" = Exchange Symbol)
    private int side;               // Tag 54 (1=Buy, 2=Sell)
    private double price;           // Tag 44
    private double quantity;        // Tag 38
    private String ordType;         // Tag 40 (default "2" = Limit)
    private String exchange;        // Tag 207 (ExDestination, e.g. TDWL)
    private String tif;             // Tag 59 (default "0" = Day)

    // D-specific
    private String handlInst;       // Tag 21 (default "1" = AutomatedPrivate)

    // G-specific
    private double minQty;          // Tag 138
}
