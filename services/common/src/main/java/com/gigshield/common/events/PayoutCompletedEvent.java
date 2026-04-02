package com.gigshield.common.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Event published when a payout is completed.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayoutCompletedEvent extends BaseEvent {
    
    public static final String TYPE = "payout.completed";
    
    private String payoutId;
    private String claimId;
    private String workerId;
    private BigDecimal amount;
    private String transactionId;
    private boolean success;
    private String reason;
}
