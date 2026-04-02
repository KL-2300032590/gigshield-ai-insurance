package com.gigshield.common.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event published when a claim is approved and ready for payout.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClaimApprovedEvent extends BaseEvent {
    
    public static final String TYPE = "claim.approved";
    
    private String claimId;
    private String policyId;
    private String workerId;
    private BigDecimal amount;
    private double fraudScore;
}
