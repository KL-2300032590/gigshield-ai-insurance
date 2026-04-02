package com.gigshield.claim.consumer;

import com.gigshield.common.events.*;
import com.gigshield.claim.service.ClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {
    
    private final ClaimService claimService;
    
    @KafkaListener(topics = KafkaTopics.ENVIRONMENT_DISRUPTION, groupId = "claim-service")
    public void handleEnvironmentDisruption(EnvironmentDisruptionEvent event) {
        log.info("Received EnvironmentDisruptionEvent: {} in {}", 
                event.getTriggerType(), event.getCity());
        
        try {
            claimService.processDisruption(event);
        } catch (Exception e) {
            log.error("Failed to process disruption event: {}", e.getMessage());
        }
    }
    
    @KafkaListener(topics = KafkaTopics.CLAIM_VALIDATED, groupId = "claim-service")
    public void handleClaimValidated(ClaimValidatedEvent event) {
        log.info("Received ClaimValidatedEvent for claim: {}", event.getClaimId());
        
        try {
            claimService.handleClaimValidation(event);
        } catch (Exception e) {
            log.error("Failed to process claim validation: {}", e.getMessage());
        }
    }
    
    @KafkaListener(topics = KafkaTopics.PAYOUT_COMPLETED, groupId = "claim-service")
    public void handlePayoutCompleted(PayoutCompletedEvent event) {
        log.info("Received PayoutCompletedEvent for claim: {}", event.getClaimId());
        
        try {
            claimService.handlePayoutCompleted(event);
        } catch (Exception e) {
            log.error("Failed to process payout completion: {}", e.getMessage());
        }
    }
}
