package com.gigshield.risk.service;

import com.gigshield.common.dto.RiskPremiumResponse;
import com.gigshield.common.model.RiskScore;
import com.gigshield.risk.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCalculationService {
    
    private final RiskScoreRepository riskScoreRepository;
    
    @Value("${risk.base-premium:20.00}")
    private BigDecimal basePremium;
    
    @Value("${risk.min-premium:15.00}")
    private BigDecimal minPremium;
    
    @Value("${risk.max-premium:50.00}")
    private BigDecimal maxPremium;
    
    @Value("${risk.default-coverage:800.00}")
    private BigDecimal defaultCoverage;
    
    public RiskPremiumResponse calculateRiskPremium(String workerId, String city, 
                                                     double latitude, double longitude) {
        log.info("Calculating risk premium for worker {} in {}", workerId, city);
        
        // Check for cached risk score
        Optional<RiskScore> cachedScore = riskScoreRepository
                .findByWorkerIdAndExpiresAtAfter(workerId, Instant.now());
        
        RiskScore riskScore;
        if (cachedScore.isPresent()) {
            riskScore = cachedScore.get();
            log.debug("Using cached risk score for worker {}", workerId);
        } else {
            riskScore = calculateAndSaveRiskScore(workerId, city, latitude, longitude);
        }
        
        BigDecimal premium = calculatePremium(riskScore.getOverallScore());
        
        return RiskPremiumResponse.builder()
                .workerId(workerId)
                .riskScore(riskScore.getOverallScore())
                .premium(premium)
                .coverageLimit(defaultCoverage)
                .factors(RiskPremiumResponse.RiskFactorsDto.builder()
                        .rainfallRisk(riskScore.getFactors().getRainfallRisk())
                        .aqiRisk(riskScore.getFactors().getAqiRisk())
                        .floodRisk(riskScore.getFactors().getFloodRisk())
                        .temperatureRisk(riskScore.getFactors().getTemperatureRisk())
                        .seasonalFactor(riskScore.getFactors().getSeasonalFactor())
                        .build())
                .build();
    }
    
    private RiskScore calculateAndSaveRiskScore(String workerId, String city, 
                                                 double latitude, double longitude) {
        // AI Risk Model - Calculate individual risk factors
        RiskScore.RiskFactors factors = calculateRiskFactors(city, latitude, longitude);
        
        // Calculate overall score (weighted average)
        double overallScore = calculateOverallScore(factors);
        
        RiskScore riskScore = RiskScore.builder()
                .workerId(workerId)
                .city(city)
                .latitude(latitude)
                .longitude(longitude)
                .overallScore(overallScore)
                .factors(factors)
                .calculatedAt(Instant.now())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        
        riskScore = riskScoreRepository.save(riskScore);
        log.info("Calculated and saved risk score {} for worker {}", overallScore, workerId);
        
        return riskScore;
    }
    
    private RiskScore.RiskFactors calculateRiskFactors(String city, double latitude, double longitude) {
        // Simulated AI model based on location and historical data
        // In production, this would integrate with ML models and historical weather data
        
        double rainfallRisk = calculateRainfallRisk(city, latitude);
        double aqiRisk = calculateAqiRisk(city);
        double floodRisk = calculateFloodRisk(city, latitude, longitude);
        double temperatureRisk = calculateTemperatureRisk(city, latitude);
        double seasonalFactor = calculateSeasonalFactor();
        
        return RiskScore.RiskFactors.builder()
                .rainfallRisk(rainfallRisk)
                .aqiRisk(aqiRisk)
                .floodRisk(floodRisk)
                .temperatureRisk(temperatureRisk)
                .seasonalFactor(seasonalFactor)
                .build();
    }
    
    private double calculateRainfallRisk(String city, double latitude) {
        // Higher risk for coastal and monsoon-prone areas
        double baseRisk = 0.3;
        
        if (city != null) {
            String cityLower = city.toLowerCase();
            if (cityLower.contains("mumbai") || cityLower.contains("chennai") || 
                cityLower.contains("kolkata")) {
                baseRisk = 0.7;
            } else if (cityLower.contains("bangalore") || cityLower.contains("hyderabad")) {
                baseRisk = 0.5;
            } else if (cityLower.contains("delhi") || cityLower.contains("jaipur")) {
                baseRisk = 0.3;
            }
        }
        
        // Adjust based on latitude (tropical regions have higher risk)
        if (latitude < 15) baseRisk += 0.1;
        
        return Math.min(1.0, baseRisk);
    }
    
    private double calculateAqiRisk(String city) {
        // Higher risk for polluted cities
        if (city == null) return 0.3;
        
        String cityLower = city.toLowerCase();
        if (cityLower.contains("delhi") || cityLower.contains("gurgaon") || 
            cityLower.contains("noida")) {
            return 0.8;
        } else if (cityLower.contains("mumbai") || cityLower.contains("kolkata")) {
            return 0.5;
        } else if (cityLower.contains("bangalore") || cityLower.contains("chennai")) {
            return 0.3;
        }
        
        return 0.4;
    }
    
    private double calculateFloodRisk(String city, double latitude, double longitude) {
        // Based on flood-prone areas
        if (city == null) return 0.2;
        
        String cityLower = city.toLowerCase();
        if (cityLower.contains("mumbai") || cityLower.contains("chennai") || 
            cityLower.contains("kolkata") || cityLower.contains("patna")) {
            return 0.6;
        }
        
        return 0.2;
    }
    
    private double calculateTemperatureRisk(String city, double latitude) {
        // Higher risk for extreme temperature areas
        if (city == null) return 0.3;
        
        String cityLower = city.toLowerCase();
        if (cityLower.contains("delhi") || cityLower.contains("jaipur") || 
            cityLower.contains("nagpur")) {
            return 0.6;  // Hot summers
        } else if (cityLower.contains("srinagar") || cityLower.contains("shimla")) {
            return 0.5;  // Cold winters
        }
        
        return 0.3;
    }
    
    private double calculateSeasonalFactor() {
        // Seasonal adjustment based on current month
        int month = java.time.LocalDate.now().getMonthValue();
        
        // Monsoon season (June-September) - higher risk
        if (month >= 6 && month <= 9) {
            return 1.3;
        }
        // Winter (November-February) - moderate risk for cold/pollution
        else if (month >= 11 || month <= 2) {
            return 1.1;
        }
        // Summer (March-May) - heat risk
        else if (month >= 3 && month <= 5) {
            return 1.2;
        }
        
        return 1.0;
    }
    
    private double calculateOverallScore(RiskScore.RiskFactors factors) {
        // Weighted average of risk factors
        double weightedSum = 
                factors.getRainfallRisk() * 0.25 +
                factors.getAqiRisk() * 0.20 +
                factors.getFloodRisk() * 0.20 +
                factors.getTemperatureRisk() * 0.15 +
                (factors.getSeasonalFactor() - 1.0) * 0.20;
        
        // Apply seasonal multiplier
        double overallScore = Math.min(1.0, Math.max(0.0, weightedSum * factors.getSeasonalFactor()));
        
        return Math.round(overallScore * 100.0) / 100.0;
    }
    
    private BigDecimal calculatePremium(double riskScore) {
        // Premium = Base + (Risk Score * Range)
        BigDecimal range = maxPremium.subtract(minPremium);
        BigDecimal riskAdjustment = range.multiply(BigDecimal.valueOf(riskScore));
        BigDecimal premium = basePremium.add(riskAdjustment);
        
        // Ensure within bounds
        premium = premium.max(minPremium).min(maxPremium);
        
        return premium.setScale(2, RoundingMode.HALF_UP);
    }
}
