package com.gigshield.simulator.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminClaimDto {
    private String id;
    private String policyId;
    private String workerId;
    private String triggerType;
    private String status;
    private Integer amount;
    private String city;
    private Instant createdAt;
    private Instant updatedAt;
}
