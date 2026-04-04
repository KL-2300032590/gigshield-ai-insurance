package com.parametrix.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Policy domain model representing a weekly micro-insurance policy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "policies")
@CompoundIndex(name = "worker_week_idx", def = "{'workerId': 1, 'weekNumber': 1, 'year': 1}", unique = true)
public class Policy {
    
    @Id
    private String id;
    
    @Indexed
    private String workerId;
    
    private int weekNumber;
    private int year;
    
    private BigDecimal premium;
    private BigDecimal coverageLimit;
    private double riskScore;
    private String city;
    private Double latitude;
    private Double longitude;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private PolicyStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    public enum PolicyStatus {
        ACTIVE,
        EXPIRED,
        CLAIMED,
        CANCELLED
    }
    
    /**
     * Check if this policy covers a specific date.
     */
    public boolean coversDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
