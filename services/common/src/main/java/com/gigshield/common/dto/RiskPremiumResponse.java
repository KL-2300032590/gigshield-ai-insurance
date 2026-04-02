package com.gigshield.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for risk score and premium calculation response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskPremiumResponse {
    
    private String workerId;
    private double riskScore;
    private BigDecimal premium;
    private BigDecimal coverageLimit;
    private RiskFactorsDto factors;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskFactorsDto {
        private double rainfallRisk;
        private double aqiRisk;
        private double floodRisk;
        private double temperatureRisk;
        private double seasonalFactor;
    }
}
