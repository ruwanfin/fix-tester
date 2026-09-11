package com.finexa.fixtester.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A named, reusable snapshot of one tab's form values.
 *
 * <p>{@code data} is stored as a free-form map so a preset keeps working when new fields are added
 * to a tab: unknown keys are ignored on load and missing keys simply leave the field untouched.</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Preset {
    private String name;         // unique within a category
    private String category;     // execution, scenario, placeorder, optionorder, bulkorder, fixdma
    private String description;  // optional free text
    private String savedAt;      // ISO-8601, set by the server on save
    private Map<String, Object> data = new LinkedHashMap<>();
}
