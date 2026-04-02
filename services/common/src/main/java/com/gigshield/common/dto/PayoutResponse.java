package com.gigshield.common.dto;

import com.gigshield.common.model.Payout;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for payout response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {
    
    private String id;
    private String claimId;
    private String workerId;
    private BigDecimal amount;
    private String transactionId;
    private String paymentMethod;
    private Payout.PayoutStatus status;
    private String statusReason;
    private Instant initiatedAt;
    private Instant completedAt;
    private Instant createdAt;
    
    public static PayoutResponse fromPayout(Payout payout) {
        return PayoutResponse.builder()
                .id(payout.getId())
                .claimId(payout.getClaimId())
                .workerId(payout.getWorkerId())
                .amount(payout.getAmount())
                .transactionId(payout.getTransactionId())
                .paymentMethod(payout.getPaymentMethod())
                .status(payout.getStatus())
                .statusReason(payout.getStatusReason())
                .initiatedAt(payout.getInitiatedAt())
                .completedAt(payout.getCompletedAt())
                .createdAt(payout.getCreatedAt())
                .build();
    }
}
