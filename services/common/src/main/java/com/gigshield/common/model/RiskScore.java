package com.gigshield.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * RiskScore domain model storing calculated risk scores.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "risk_scores")
public class RiskScore {
    
    @Id
    private String id;
    
    @Indexed
    private String workerId;
    
    private String city;
    private double latitude;
    private double longitude;
    
    private double overallScore;        // 0.0 to 1.0
    
    private RiskFactors factors;
    
    private Instant calculatedAt;
    private Instant expiresAt;          // Risk scores are recalculated periodically
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskFactors {
        private double rainfallRisk;     // Historical rainfall risk
        private double aqiRisk;          // Air quality risk
        private double floodRisk;        // Flood probability
        private double temperatureRisk;  // Extreme temperature risk
        private double seasonalFactor;   // Seasonal adjustment
    }
}
