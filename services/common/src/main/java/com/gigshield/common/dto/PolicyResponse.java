package com.gigshield.common.dto;

import com.gigshield.common.model.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for policy response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    
    private String id;
    private String workerId;
    private int weekNumber;
    private int year;
    private BigDecimal premium;
    private BigDecimal coverageLimit;
    private double riskScore;
    private LocalDate startDate;
    private LocalDate endDate;
    private Policy.PolicyStatus status;
    private Instant createdAt;
    
    public static PolicyResponse fromPolicy(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .workerId(policy.getWorkerId())
                .weekNumber(policy.getWeekNumber())
                .year(policy.getYear())
                .premium(policy.getPremium())
                .coverageLimit(policy.getCoverageLimit())
                .riskScore(policy.getRiskScore())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus())
                .createdAt(policy.getCreatedAt())
                .build();
    }
}
