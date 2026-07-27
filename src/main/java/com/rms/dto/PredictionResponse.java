package com.rms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {
    private String category;                 // Excellent / Good / Average / Needs Improvement / At Risk

    private Double confidence;

    @JsonProperty("class_probabilities")
    private Map<String, Double> classProbabilities;

    @JsonProperty("quick_category")
    private String quickCategory;             // High Performer / Average Performer / Needs Attention / Critical Intervention

    private String error;                     // populated only on failure
}