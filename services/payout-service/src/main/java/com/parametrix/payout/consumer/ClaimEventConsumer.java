package com.parametrix.payout.consumer;

import com.parametrix.common.events.ClaimApprovedEvent;
import com.parametrix.common.events.KafkaTopics;
import com.parametrix.payout.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimEventConsumer {
    
    private final PayoutService payoutService;
    
    @KafkaListener(topics = KafkaTopics.CLAIM_APPROVED, groupId = "payout-service")
    public void handleClaimApproved(ClaimApprovedEvent event) {
        log.info("Received ClaimApprovedEvent for claim: {}", event.getClaimId());
        
        try {
            payoutService.processApprovedClaim(event);
        } catch (Exception e) {
            log.error("Failed to process approved claim {}: {}", event.getClaimId(), e.getMessage());
        }
    }
}
