package com.gigshield.payout.service;

import com.gigshield.common.dto.PayoutResponse;
import com.gigshield.common.events.ClaimApprovedEvent;
import com.gigshield.common.events.KafkaTopics;
import com.gigshield.common.events.PayoutCompletedEvent;
import com.gigshield.common.model.Claim;
import com.gigshield.common.model.Payout;
import com.gigshield.common.model.Worker;
import com.gigshield.payout.client.RazorpayClient;
import com.gigshield.payout.repository.ClaimRepository;
import com.gigshield.payout.repository.PayoutRepository;
import com.gigshield.payout.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    
    private final PayoutRepository payoutRepository;
    private final ClaimRepository claimRepository;
    private final WorkerRepository workerRepository;
    private final RazorpayClient razorpayClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${payment.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${payment.location.validation-radius-km:50}")
    private double locationValidationRadiusKm;
    
    public void processApprovedClaim(ClaimApprovedEvent event) {
        log.info("Processing approved claim {} for payout", event.getClaimId());
        
        // Check if payout already exists
        if (payoutRepository.existsByClaimId(event.getClaimId())) {
            log.warn("Payout already exists for claim: {}", event.getClaimId());
            return;
        }

        if (!isPayoutLocationValid(event.getClaimId(), event.getWorkerId())) {
            log.warn("Payout rejected due to location mismatch for claim {}", event.getClaimId());
            publishLocationRejectedPayoutEvent(event);
            return;
        }
        
        // Create payout record
        Payout payout = Payout.builder()
                .claimId(event.getClaimId())
                .workerId(event.getWorkerId())
                .amount(event.getAmount())
                .paymentMethod("UPI")
                .paymentDetails("worker_" + event.getWorkerId() + "@upi")  // Simulated UPI ID
                .status(Payout.PayoutStatus.INITIATED)
                .retryCount(0)
                .initiatedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        payout = payoutRepository.save(payout);
        log.info("Created payout record: {}", payout.getId());
        
        // Initiate actual payout
        initiatePayout(payout);
    }
    
    private void initiatePayout(Payout payout) {
        payout.setStatus(Payout.PayoutStatus.PROCESSING);
        payout.setUpdatedAt(Instant.now());
        payoutRepository.save(payout);
        
        razorpayClient.initiatePayout(
                payout.getWorkerId(),
                payout.getPaymentDetails(),
                payout.getAmount()
        ).subscribe(result -> {
            if (result.isSuccess()) {
                handlePayoutSuccess(payout, result.getTransactionId());
            } else {
                handlePayoutFailure(payout, result.getMessage());
            }
        });
    }
    
    private void handlePayoutSuccess(Payout payout, String transactionId) {
        log.info("Payout {} successful with transaction: {}", payout.getId(), transactionId);
        
        payout.setStatus(Payout.PayoutStatus.SUCCESS);
        payout.setTransactionId(transactionId);
        payout.setCompletedAt(Instant.now());
        payout.setUpdatedAt(Instant.now());
        payoutRepository.save(payout);
        
        // Publish completion event
        publishPayoutCompletedEvent(payout, true, "Payout completed successfully");
    }
    
    private void handlePayoutFailure(Payout payout, String reason) {
        log.warn("Payout {} failed: {}", payout.getId(), reason);
        
        payout.setRetryCount(payout.getRetryCount() + 1);
        payout.setLastRetryAt(Instant.now());
        payout.setStatusReason(reason);
        payout.setUpdatedAt(Instant.now());
        
        if (payout.getRetryCount() < maxRetryAttempts) {
            log.info("Scheduling retry {} for payout {}", payout.getRetryCount(), payout.getId());
            payout.setStatus(Payout.PayoutStatus.INITIATED);
            payoutRepository.save(payout);
            
            // Retry after delay (in production, use a scheduler)
            initiatePayout(payout);
        } else {
            log.error("Payout {} failed after {} attempts", payout.getId(), maxRetryAttempts);
            payout.setStatus(Payout.PayoutStatus.FAILED);
            payoutRepository.save(payout);
            
            // Publish failure event
            publishPayoutCompletedEvent(payout, false, reason);
        }
    }
    
    public List<PayoutResponse> getWorkerPayouts(String workerId) {
        return payoutRepository.findByWorkerIdOrderByCreatedAtDesc(workerId).stream()
                .map(PayoutResponse::fromPayout)
                .collect(Collectors.toList());
    }
    
    public PayoutResponse getPayoutById(String payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found"));
        return PayoutResponse.fromPayout(payout);
    }
    
    private void publishPayoutCompletedEvent(Payout payout, boolean success, String reason) {
        PayoutCompletedEvent event = PayoutCompletedEvent.builder()
                .eventType(PayoutCompletedEvent.TYPE)
                .payoutId(payout.getId())
                .claimId(payout.getClaimId())
                .workerId(payout.getWorkerId())
                .amount(payout.getAmount())
                .transactionId(payout.getTransactionId())
                .success(success)
                .reason(reason)
                .build();
        
        kafkaTemplate.send(KafkaTopics.PAYOUT_COMPLETED, payout.getId(), event);
        log.info("Published PayoutCompletedEvent for payout: {}", payout.getId());
    }

    private boolean isPayoutLocationValid(String claimId, String workerId) {
        Claim claim = claimRepository.findById(claimId).orElse(null);
        Worker worker = workerRepository.findById(workerId).orElse(null);
        if (claim == null || claim.getTriggerData() == null || worker == null || worker.getLocation() == null) {
            return false;
        }
        Double disruptionLat = claim.getTriggerData().getLatitude();
        Double disruptionLon = claim.getTriggerData().getLongitude();
        if (disruptionLat == null || disruptionLon == null) {
            return false;
        }

        double distanceKm = calculateDistanceKm(
                worker.getLocation().getLatitude(),
                worker.getLocation().getLongitude(),
                disruptionLat,
                disruptionLon
        );
        return distanceKm <= locationValidationRadiusKm;
    }

    private void publishLocationRejectedPayoutEvent(ClaimApprovedEvent event) {
        PayoutCompletedEvent rejectedEvent = PayoutCompletedEvent.builder()
                .eventType(PayoutCompletedEvent.TYPE)
                .claimId(event.getClaimId())
                .workerId(event.getWorkerId())
                .amount(event.getAmount())
                .success(false)
                .reason("Payout blocked: worker location does not match disruption zone")
                .build();
        kafkaTemplate.send(KafkaTopics.PAYOUT_COMPLETED, event.getClaimId(), rejectedEvent);
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
