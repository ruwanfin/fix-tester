package com.finexa.fixtester.dto;

import lombok.Data;

@Data
public class BulkOrderRequest {
    private int               orderCount      = 100;
    private PlaceOrderRequest template        = new PlaceOrderRequest();
    private boolean           useSameAccount  = false;
    private int               sameAccountID   = 0;
    private String            sameCustomerNo  = "";
}
