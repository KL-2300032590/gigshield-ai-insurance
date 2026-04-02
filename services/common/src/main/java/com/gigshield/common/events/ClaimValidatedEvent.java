package com.gigshield.common.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Event published after fraud detection validates a claim.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClaimValidatedEvent extends BaseEvent {
    
    public static final String TYPE = "claim.validated";
    
    private String claimId;
    private String policyId;
    private String workerId;
    private boolean approved;
    private double fraudScore;
    private String reason;
}
