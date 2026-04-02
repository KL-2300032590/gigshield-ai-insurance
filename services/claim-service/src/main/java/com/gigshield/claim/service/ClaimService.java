package com.gigshield.claim.service;

import com.gigshield.common.dto.ClaimResponse;
import com.gigshield.common.events.*;
import com.gigshield.common.model.Claim;
import com.gigshield.common.model.Policy;
import com.gigshield.claim.repository.ClaimRepository;
import com.gigshield.claim.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${claim.location.validation-radius-km:50}")
    private double locationValidationRadiusKm;
    
    @Transactional
    public void processDisruption(EnvironmentDisruptionEvent event) {
        log.info("Processing disruption event: {} in {}", event.getTriggerType(), event.getCity());
        
        // Find all active policies in the affected city
        List<Policy> activePolicies = policyRepository.findActivePoliciesInCity(
                event.getCity(), 
                LocalDate.now(),
                Policy.PolicyStatus.ACTIVE
        );
        
        log.info("Found {} active policies affected by disruption in {}", 
                activePolicies.size(), event.getCity());
        
        for (Policy policy : activePolicies) {
            if (!isPolicyWithinDisruptionRadius(policy, event)) {
                log.debug("Skipping policy {} due to location mismatch for disruption in {}",
                        policy.getId(), event.getCity());
                continue;
            }
            createClaimForPolicy(policy, event);
        }
    }
    
    private void createClaimForPolicy(Policy policy, EnvironmentDisruptionEvent event) {
        // Check if claim already exists for this policy and trigger
        if (claimRepository.existsByPolicyIdAndTriggerType(policy.getId(), event.getTriggerType())) {
            log.debug("Claim already exists for policy {} and trigger {}", 
                    policy.getId(), event.getTriggerType());
            return;
        }
        
        // Create claim
        Claim claim = Claim.builder()
                .policyId(policy.getId())
                .workerId(policy.getWorkerId())
                .triggerType(event.getTriggerType())
                .triggerData(Claim.TriggerData.builder()
                        .value(event.getMeasuredValue())
                        .threshold(event.getThreshold())
                        .location(event.getCity())
                        .latitude(event.getLatitude())
                        .longitude(event.getLongitude())
                        .source(event.getSource())
                        .measuredAt(event.getMeasuredAt())
                        .build())
                .amount(policy.getCoverageLimit())
                .fraudScore(0.0)
                .status(Claim.ClaimStatus.PENDING)
                .triggeredAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        claim = claimRepository.save(claim);
        log.info("Created claim {} for policy {}", claim.getId(), policy.getId());
        
        // Update claim status and publish event
        claim.setStatus(Claim.ClaimStatus.VALIDATING);
        claimRepository.save(claim);
        
        publishClaimInitiatedEvent(claim, event);
    }
    
    @Transactional
    public void handleClaimValidation(ClaimValidatedEvent event) {
        log.info("Processing claim validation result for claim {}: approved={}", 
                event.getClaimId(), event.isApproved());
        
        Claim claim = claimRepository.findById(event.getClaimId())
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + event.getClaimId()));
        
        claim.setFraudScore(event.getFraudScore());
        claim.setStatusReason(event.getReason());
        claim.setProcessedAt(Instant.now());
        claim.setUpdatedAt(Instant.now());
        
        if (event.isApproved()) {
            claim.setStatus(Claim.ClaimStatus.APPROVED);
            claimRepository.save(claim);
            
            // Publish approval event for payout
            publishClaimApprovedEvent(claim);
            
            // Update policy status
            policyRepository.findById(claim.getPolicyId()).ifPresent(policy -> {
                policy.setStatus(Policy.PolicyStatus.CLAIMED);
                policy.setUpdatedAt(Instant.now());
                policyRepository.save(policy);
            });
        } else {
            claim.setStatus(Claim.ClaimStatus.REJECTED);
            claimRepository.save(claim);
            log.info("Claim {} rejected: {}", claim.getId(), event.getReason());
        }
    }
    
    @Transactional
    public void handlePayoutCompleted(PayoutCompletedEvent event) {
        log.info("Processing payout completion for claim {}: success={}", 
                event.getClaimId(), event.isSuccess());
        
        Claim claim = claimRepository.findById(event.getClaimId())
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + event.getClaimId()));
        
        if (event.isSuccess()) {
            claim.setStatus(Claim.ClaimStatus.PAID);
        } else {
            claim.setStatus(Claim.ClaimStatus.FAILED);
            claim.setStatusReason(event.getReason());
        }
        
        claim.setUpdatedAt(Instant.now());
        claimRepository.save(claim);
    }
    
    public List<ClaimResponse> getWorkerClaims(String workerId) {
        return claimRepository.findByWorkerIdOrderByCreatedAtDesc(workerId).stream()
                .map(ClaimResponse::fromClaim)
                .collect(Collectors.toList());
    }
    
    public ClaimResponse getClaimById(String claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        return ClaimResponse.fromClaim(claim);
    }
    
    private void publishClaimInitiatedEvent(Claim claim, EnvironmentDisruptionEvent triggerEvent) {
        ClaimInitiatedEvent event = ClaimInitiatedEvent.builder()
                .eventType(ClaimInitiatedEvent.TYPE)
                .claimId(claim.getId())
                .policyId(claim.getPolicyId())
                .workerId(claim.getWorkerId())
                .triggerType(claim.getTriggerType())
                .measuredValue(triggerEvent.getMeasuredValue())
                .threshold(triggerEvent.getThreshold())
                .city(triggerEvent.getCity())
                .disruptionLatitude(triggerEvent.getLatitude())
                .disruptionLongitude(triggerEvent.getLongitude())
                .amount(claim.getAmount())
                .build();
        
        kafkaTemplate.send(KafkaTopics.CLAIM_INITIATED, claim.getId(), event);
        log.info("Published ClaimInitiatedEvent for claim: {}", claim.getId());
    }
    
    private void publishClaimApprovedEvent(Claim claim) {
        ClaimApprovedEvent event = ClaimApprovedEvent.builder()
                .eventType(ClaimApprovedEvent.TYPE)
                .claimId(claim.getId())
                .policyId(claim.getPolicyId())
                .workerId(claim.getWorkerId())
                .amount(claim.getAmount())
                .fraudScore(claim.getFraudScore())
                .build();
        
        kafkaTemplate.send(KafkaTopics.CLAIM_APPROVED, claim.getId(), event);
        log.info("Published ClaimApprovedEvent for claim: {}", claim.getId());
    }

    private boolean isPolicyWithinDisruptionRadius(Policy policy, EnvironmentDisruptionEvent event) {
        if (policy.getLatitude() == null || policy.getLongitude() == null) {
            return false;
        }
        return calculateDistanceKm(
                policy.getLatitude(),
                policy.getLongitude(),
                event.getLatitude(),
                event.getLongitude()
        ) <= locationValidationRadiusKm;
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
