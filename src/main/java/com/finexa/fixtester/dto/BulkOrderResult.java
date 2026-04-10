package com.finexa.fixtester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkOrderResult {
    private int    index;
    private int    tradingAccountID;
    private String customerNo;
    private String symbol;
    private boolean success;
    private String  message;
    private long    rttMs;
}
