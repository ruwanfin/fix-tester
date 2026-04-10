package com.finexa.fixtester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDataEntry {
    private int    tradeAccId;
    private String customerNo;
}
