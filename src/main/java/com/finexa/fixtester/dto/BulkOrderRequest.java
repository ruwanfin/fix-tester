package com.finexa.fixtester.dto;

import lombok.Data;

@Data
public class BulkOrderRequest {
    private int              orderCount = 100;
    private PlaceOrderRequest template  = new PlaceOrderRequest();
}
