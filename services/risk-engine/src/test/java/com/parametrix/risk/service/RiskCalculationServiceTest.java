package com.parametrix.risk.service;

import com.parametrix.common.dto.RiskPremiumResponse;
import com.parametrix.common.model.RiskScore;
import com.parametrix.risk.repository.RiskScoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskCalculationServiceTest {

    @Mock
    private RiskScoreRepository riskScoreRepository;

    @InjectMocks
    private RiskCalculationService riskCalculationService;

    @Test
    void calculateRiskPremium_MumbaiHighRisk() {
        // Setup
        ReflectionTestUtils.setField(riskCalculationService, "basePremium", new BigDecimal("20.00"));
        ReflectionTestUtils.setField(riskCalculationService, "minPremium", new BigDecimal("15.00"));
        ReflectionTestUtils.setField(riskCalculationService, "maxPremium", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(riskCalculationService, "defaultCoverage", new BigDecimal("800.00"));

        when(riskScoreRepository.findByWorkerIdAndExpiresAtAfter(any(), any()))
                .thenReturn(Optional.empty());
        when(riskScoreRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Execute
        RiskPremiumResponse response = riskCalculationService.calculateRiskPremium(
                "worker123", "Mumbai", 19.076, 72.877);

        // Verify
        assertThat(response).isNotNull();
        assertThat(response.getWorkerId()).isEqualTo("worker123");
        assertThat(response.getRiskScore()).isGreaterThan(0.0);
        assertThat(response.getRiskScore()).isLessThanOrEqualTo(1.0);
        assertThat(response.getPremium()).isGreaterThanOrEqualTo(new BigDecimal("15.00"));
        assertThat(response.getPremium()).isLessThanOrEqualTo(new BigDecimal("50.00"));
        assertThat(response.getCoverageLimit()).isEqualTo(new BigDecimal("800.00"));
        assertThat(response.getFactors()).isNotNull();
    }

    @Test
    void calculateRiskPremium_DelhiHighPollution() {
        // Setup
        ReflectionTestUtils.setField(riskCalculationService, "basePremium", new BigDecimal("20.00"));
        ReflectionTestUtils.setField(riskCalculationService, "minPremium", new BigDecimal("15.00"));
        ReflectionTestUtils.setField(riskCalculationService, "maxPremium", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(riskCalculationService, "defaultCoverage", new BigDecimal("800.00"));

        when(riskScoreRepository.findByWorkerIdAndExpiresAtAfter(any(), any()))
                .thenReturn(Optional.empty());
        when(riskScoreRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Execute
        RiskPremiumResponse response = riskCalculationService.calculateRiskPremium(
                "worker456", "Delhi", 28.6139, 77.209);

        // Verify
        assertThat(response).isNotNull();
        assertThat(response.getFactors().getAqiRisk()).isGreaterThan(0.5); // Delhi has high AQI risk
    }

    @Test
    void calculateRiskPremium_UseCachedScore() {
        // Setup
        RiskScore cachedScore = RiskScore.builder()
                .workerId("worker123")
                .city("Mumbai")
                .overallScore(0.65)
                .factors(RiskScore.RiskFactors.builder()
                        .rainfallRisk(0.7)
                        .aqiRisk(0.5)
                        .floodRisk(0.6)
                        .temperatureRisk(0.3)
                        .seasonalFactor(1.2)
                        .build())
                .build();

        ReflectionTestUtils.setField(riskCalculationService, "basePremium", new BigDecimal("20.00"));
        ReflectionTestUtils.setField(riskCalculationService, "minPremium", new BigDecimal("15.00"));
        ReflectionTestUtils.setField(riskCalculationService, "maxPremium", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(riskCalculationService, "defaultCoverage", new BigDecimal("800.00"));

        when(riskScoreRepository.findByWorkerIdAndExpiresAtAfter(any(), any()))
                .thenReturn(Optional.of(cachedScore));

        // Execute
        RiskPremiumResponse response = riskCalculationService.calculateRiskPremium(
                "worker123", "Mumbai", 19.076, 72.877);

        // Verify
        assertThat(response.getRiskScore()).isEqualTo(0.65);
    }
}
