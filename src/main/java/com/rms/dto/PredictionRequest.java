package com.rms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionRequest {
    private Long studentId;       // optional, for tagging history in the UI
    private Double attendance;        // 0-100
    private Double assignmentScore;   // 0-100
    private Double midtermScore;      // 0-100
    private Double participation;     // 0-100
    private Double prevGpa;           // 0-4
}