package com.parametrix.fraud.consumer;

import com.parametrix.common.events.ClaimInitiatedEvent;
import com.parametrix.common.events.KafkaTopics;
import com.parametrix.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimEventConsumer {
    
    private final FraudDetectionService fraudDetectionService;
    
    @KafkaListener(topics = KafkaTopics.CLAIM_INITIATED, groupId = "fraud-detection")
    public void handleClaimInitiated(ClaimInitiatedEvent event) {
        log.info("Received ClaimInitiatedEvent for claim: {}", event.getClaimId());
        
        try {
            fraudDetectionService.validateClaim(event);
        } catch (Exception e) {
            log.error("Failed to validate claim {}: {}", event.getClaimId(), e.getMessage());
        }
    }
}
