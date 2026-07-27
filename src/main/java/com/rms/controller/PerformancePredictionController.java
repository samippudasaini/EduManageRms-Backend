package com.rms.controller;

import com.rms.dto.PredictionRequest;
import com.rms.dto.PredictionResponse;
import com.rms.service.PerformancePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@CrossOrigin
public class PerformancePredictionController {

    private final PerformancePredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@RequestBody PredictionRequest request) {
        PredictionResponse response = predictionService.predict(request);
        if (response.getError() != null) {
            return ResponseEntity.status(502).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean up = predictionService.isServiceHealthy();
        return ResponseEntity.ok(Map.of("mlServiceHealthy", up));
    }

    @PostMapping("/retrain")
    public ResponseEntity<Map<String, Object>> retrain(@RequestBody(required = false) Map<String, String> body) {
        String dataCsvPath = body != null ? body.get("dataCsvPath") : null;
        return ResponseEntity.ok(predictionService.retrain(dataCsvPath));
    }
}