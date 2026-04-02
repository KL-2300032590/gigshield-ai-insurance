package com.gigshield.common.events;

import com.gigshield.common.model.Claim.TriggerType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event published when a claim is initiated.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClaimInitiatedEvent extends BaseEvent {
    
    public static final String TYPE = "claim.initiated";
    
    private String claimId;
    private String policyId;
    private String workerId;
    private TriggerType triggerType;
    private double measuredValue;
    private double threshold;
    private String city;
    private double disruptionLatitude;
    private double disruptionLongitude;
    private BigDecimal amount;
}
