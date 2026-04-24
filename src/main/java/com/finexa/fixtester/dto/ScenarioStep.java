package com.finexa.fixtester.dto;

import lombok.Data;

@Data
public class ScenarioStep {
    private String execType;      // NEW, PARTIAL_FILL, FILL, REPLACED, CANCELED, REJECTED
    private double fillQty;       // Tag 32 - Last Quantity
    private double fillPrice;     // Tag 31 - Last Price
    private double cumQty;        // Tag 14 - Cumulative Quantity
    private double leavesQty;     // Tag 151 - Leaves Quantity
    private double avgPrice;      // Tag 6  - Average Price
    private String execId;        // Tag 17 - Exec ID (auto-generated if empty)
    private int msgSeqNum;        // Tag 34 - Msg Seq Num (auto-incremented if 0)
    private String orderId;       // Tag 37 - Per-step orderId override (uses base if empty)
    private String clOrdId;       // Tag 11 - Per-step clOrdId override (uses base if empty)
    private String origClOrdId;   // Tag 41 - Original ClOrdID for amend/cancel steps
}
