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
 * Payout domain model representing a payment to a worker.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payouts")
public class Payout {
    
    @Id
    private String id;
    
    @Indexed
    private String claimId;
    
    @Indexed
    private String workerId;
    
    private BigDecimal amount;
    
    private String transactionId;       // Payment gateway transaction ID
    private String paymentMethod;       // UPI, BANK_TRANSFER
    private String paymentDetails;      // UPI ID or bank account (masked)
    
    private PayoutStatus status;
    private String statusReason;
    
    private int retryCount;
    private Instant lastRetryAt;
    
    private Instant initiatedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
    
    public enum PayoutStatus {
        INITIATED,
        PROCESSING,
        SUCCESS,
        FAILED,
        CANCELLED
    }
}
