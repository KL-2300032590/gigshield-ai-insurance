package com.gigshield.common.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Event published when a worker purchases a new policy.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PolicyPurchasedEvent extends BaseEvent {
    
    public static final String TYPE = "policy.purchased";
    
    private String policyId;
    private String workerId;
    private int weekNumber;
    private int year;
    private BigDecimal premium;
    private BigDecimal coverageLimit;
    private double riskScore;
    private LocalDate startDate;
    private LocalDate endDate;
}
