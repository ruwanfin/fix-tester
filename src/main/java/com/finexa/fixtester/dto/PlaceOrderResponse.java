package com.finexa.fixtester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderResponse {
    private boolean success;
    private String  message;

    // gRPC envelope
    private int     statusCode;

    // OrderResponseBodyGRPC
    private int     status;
    private String  reference;
    private String  statusDescription;
    private int     errorCode;
    private String  errorMessage;
    private String  errorDetail;

    // Performance
    private long    rttMs;
}
