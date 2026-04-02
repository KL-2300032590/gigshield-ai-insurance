package com.gigshield.fraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fraud_checks")
public class FraudCheck {
    
    @Id
    private String id;
    
    @Indexed
    private String claimId;
    
    @Indexed
    private String workerId;
    
    private double fraudScore;
    private List<String> flags;
    private boolean approved;
    private String reason;
    private Instant checkedAt;
}
