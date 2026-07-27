package com.rms.service;

import com.rms.dto.PredictionRequest;
import com.rms.dto.PredictionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Talks to the Python Flask ML microservice (see ml_service/predict_api.py)
 * which wraps the trained RandomForestClassifier.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PerformancePredictionService {

    private final RestTemplate restTemplate;

    @Value("${ml.service.base-url:http://localhost:5001}")
    private String mlServiceBaseUrl;

    public PredictionResponse predict(PredictionRequest req) {
        Map<String, Object> payload = toFeatureMap(req);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            return restTemplate.postForObject(
                    mlServiceBaseUrl + "/predict", entity, PredictionResponse.class
            );
        } catch (HttpStatusCodeException e) {
            log.warn("ML service returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return PredictionResponse.builder()
                    .error("Prediction failed: " + e.getStatusCode())
                    .build();
        } catch (ResourceAccessException e) {
            log.error("ML service unreachable at {}", mlServiceBaseUrl, e);
            return PredictionResponse.builder()
                    .error("ML service is unreachable. Is predict_api.py running on " + mlServiceBaseUrl + "?")
                    .build();
        }
    }

    public boolean isServiceHealthy() {
        try {
            restTemplate.getForObject(mlServiceBaseUrl + "/health", Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> retrain(String dataCsvPath) {
        Map<String, Object> body = new HashMap<>();
        if (dataCsvPath != null && !dataCsvPath.isBlank()) {
            body.put("data_csv_path", dataCsvPath);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(mlServiceBaseUrl + "/retrain", entity, Map.class);
    }

    private Map<String, Object> toFeatureMap(PredictionRequest req) {
        Map<String, Object> m = new HashMap<>();
        m.put("attendance", req.getAttendance());
        m.put("assignment_score", req.getAssignmentScore());
        m.put("midterm_score", req.getMidtermScore());
        m.put("participation", req.getParticipation());
        m.put("prev_gpa", req.getPrevGpa());
        return m;
    }
}