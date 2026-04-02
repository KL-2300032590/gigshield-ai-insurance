package com.gigshield.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for policy purchase request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPurchaseRequest {
    
    private String workerId;
    private int weekNumber;
    private int year;
    private BigDecimal coverageLimit;  // Optional: defaults to standard coverage
}
