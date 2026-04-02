package com.finexa.fixtester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScenarioResponse {
    private boolean success;
    private String message;
    private List<StepResult> results;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StepResult {
        private int stepIndex;
        private String execType;
        private boolean success;
        private String message;
        private String rawMessage;
        private String topic;
        private Long offset;
        private Integer partition;
    }
}
