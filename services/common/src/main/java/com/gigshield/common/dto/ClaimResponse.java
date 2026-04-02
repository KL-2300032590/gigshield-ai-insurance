package com.gigshield.common.dto;

import com.gigshield.common.model.Claim;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for claim response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    
    private String id;
    private String policyId;
    private String workerId;
    private Claim.TriggerType triggerType;
    private TriggerDataDto triggerData;
    private BigDecimal amount;
    private Claim.ClaimStatus status;
    private String statusReason;
    private Instant triggeredAt;
    private Instant processedAt;
    private Instant createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerDataDto {
        private double value;
        private double threshold;
        private String location;
        private String source;
        private Instant measuredAt;
    }
    
    public static ClaimResponse fromClaim(Claim claim) {
        TriggerDataDto triggerDataDto = null;
        if (claim.getTriggerData() != null) {
            triggerDataDto = TriggerDataDto.builder()
                    .value(claim.getTriggerData().getValue())
                    .threshold(claim.getTriggerData().getThreshold())
                    .location(claim.getTriggerData().getLocation())
                    .source(claim.getTriggerData().getSource())
                    .measuredAt(claim.getTriggerData().getMeasuredAt())
                    .build();
        }
        
        return ClaimResponse.builder()
                .id(claim.getId())
                .policyId(claim.getPolicyId())
                .workerId(claim.getWorkerId())
                .triggerType(claim.getTriggerType())
                .triggerData(triggerDataDto)
                .amount(claim.getAmount())
                .status(claim.getStatus())
                .statusReason(claim.getStatusReason())
                .triggeredAt(claim.getTriggeredAt())
                .processedAt(claim.getProcessedAt())
                .createdAt(claim.getCreatedAt())
                .build();
    }
}
