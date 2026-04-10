package com.finexa.fixtester.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkOrderResponse {
    private int                  total;
    private int                  succeeded;
    private int                  failed;
    private List<BulkOrderResult> results;
}
