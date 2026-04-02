package com.gigshield.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Claim domain model representing an insurance claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "claims")
public class Claim {
    
    @Id
    private String id;
    
    @Indexed
    private String policyId;
    
    @Indexed
    private String workerId;
    
    private TriggerType triggerType;
    private TriggerData triggerData;
    
    private BigDecimal amount;
    private double fraudScore;
    
    private ClaimStatus status;
    private String statusReason;
    
    private Instant triggeredAt;
    private Instant processedAt;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerData {
        private double value;           // Actual measured value (rainfall mm, AQI, temp)
        private double threshold;       // Threshold that was exceeded
        private String location;        // City/area where measured
        private String source;          // Data source (API name)
        private Instant measuredAt;     // When the measurement was taken
    }
    
    public enum TriggerType {
        HEAVY_RAIN,
        FLOOD,
        AIR_POLLUTION,
        EXTREME_HEAT,
        EXTREME_COLD
    }
    
    public enum ClaimStatus {
        PENDING,            // Initial state
        VALIDATING,         // Fraud detection in progress
        APPROVED,           // Passed fraud check
        REJECTED,           // Failed fraud check
        PAID,               // Payout completed
        FAILED              // Payout failed
    }
}
