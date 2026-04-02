package com.finexa.fixtester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageResponse {
    private boolean success;
    private String message;
    private String rawMessage;
    private String topic;
    private Long offset;
    private Integer partition;
}
