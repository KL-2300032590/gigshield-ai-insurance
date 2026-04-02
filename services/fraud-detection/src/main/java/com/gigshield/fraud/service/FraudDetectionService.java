package com.gigshield.fraud.service;

import com.gigshield.common.events.ClaimInitiatedEvent;
import com.gigshield.common.events.ClaimValidatedEvent;
import com.gigshield.common.events.KafkaTopics;
import com.gigshield.common.model.Worker;
import com.gigshield.fraud.model.FraudCheck;
import com.gigshield.fraud.repository.FraudCheckRepository;
import com.gigshield.fraud.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    
    private final FraudCheckRepository fraudCheckRepository;
    private final WorkerRepository workerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${fraud.thresholds.fraud-score-max:0.7}")
    private double fraudScoreThreshold;
    
    @Value("${fraud.thresholds.duplicate-claim-hours:24}")
    private int duplicateClaimHours;

    @Value("${fraud.thresholds.location-radius-km:50}")
    private double locationRadiusKm;
    
    public void validateClaim(ClaimInitiatedEvent event) {
        log.info("Validating claim {} for worker {}", event.getClaimId(), event.getWorkerId());
        
        List<String> flags = new ArrayList<>();
        double fraudScore = 0.0;
        
        // Check 1: Duplicate claims
        if (hasDuplicateClaim(event.getWorkerId(), event.getClaimId())) {
            flags.add("DUPLICATE_CLAIM");
            fraudScore += 0.4;
        }
        
        // Check 2: Claim frequency anomaly
        double frequencyScore = checkClaimFrequency(event.getWorkerId());
        if (frequencyScore > 0) {
            flags.add("HIGH_CLAIM_FREQUENCY");
            fraudScore += frequencyScore;
        }
        
        // Check 3: Amount anomaly (if significantly higher than average)
        double amountScore = checkAmountAnomaly(event.getAmount().doubleValue());
        if (amountScore > 0) {
            flags.add("AMOUNT_ANOMALY");
            fraudScore += amountScore;
        }

        // Check 4: Worker location vs disruption location
        double locationScore = checkLocationMismatch(event);
        if (locationScore > 0) {
            flags.add("LOCATION_MISMATCH");
            fraudScore += locationScore;
        }
        
        // Normalize score to 0-1
        fraudScore = Math.min(1.0, fraudScore);
        
        // Determine approval
        boolean approved = fraudScore < fraudScoreThreshold;
        String reason = approved ? "Claim validated successfully" : 
                "Fraud score too high: " + String.join(", ", flags);
        
        // Save fraud check result
        FraudCheck fraudCheck = FraudCheck.builder()
                .claimId(event.getClaimId())
                .workerId(event.getWorkerId())
                .fraudScore(fraudScore)
                .flags(flags)
                .approved(approved)
                .reason(reason)
                .checkedAt(Instant.now())
                .build();
        
        fraudCheckRepository.save(fraudCheck);
        
        // Publish validation result
        publishValidationResult(event, approved, fraudScore, reason);
        
        log.info("Claim {} validation complete: approved={}, fraudScore={}", 
                event.getClaimId(), approved, fraudScore);
    }
    
    private boolean hasDuplicateClaim(String workerId, String currentClaimId) {
        Instant since = Instant.now().minus(duplicateClaimHours, ChronoUnit.HOURS);
        List<FraudCheck> recentChecks = fraudCheckRepository
                .findByWorkerIdAndCheckedAtAfter(workerId, since);
        
        return recentChecks.stream()
                .anyMatch(check -> !check.getClaimId().equals(currentClaimId));
    }
    
    private double checkClaimFrequency(String workerId) {
        // Check claims in last 30 days
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        long claimCount = fraudCheckRepository.countByWorkerIdAndCheckedAtAfter(workerId, since);
        
        // More than 4 claims per month is suspicious
        if (claimCount > 4) {
            return 0.3;
        } else if (claimCount > 2) {
            return 0.1;
        }
        return 0.0;
    }
    
    private double checkAmountAnomaly(double amount) {
        // Flag if amount is significantly higher than typical coverage
        // Typical coverage is ₹800
        if (amount > 1000) {
            return 0.2;
        }
        return 0.0;
    }

    private double checkLocationMismatch(ClaimInitiatedEvent event) {
        return workerRepository.findById(event.getWorkerId())
                .map(Worker::getLocation)
                .map(location -> {
                    if (location == null) {
                        return 0.5;
                    }
                    double distanceKm = calculateDistanceKm(
                            location.getLatitude(),
                            location.getLongitude(),
                            event.getDisruptionLatitude(),
                            event.getDisruptionLongitude()
                    );
                    return distanceKm > locationRadiusKm ? 0.5 : 0.0;
                })
                .orElse(0.5);
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
    
    private void publishValidationResult(ClaimInitiatedEvent event, boolean approved, 
                                          double fraudScore, String reason) {
        ClaimValidatedEvent validatedEvent = ClaimValidatedEvent.builder()
                .eventType(ClaimValidatedEvent.TYPE)
                .claimId(event.getClaimId())
                .policyId(event.getPolicyId())
                .workerId(event.getWorkerId())
                .approved(approved)
                .fraudScore(fraudScore)
                .reason(reason)
                .build();
        
        kafkaTemplate.send(KafkaTopics.CLAIM_VALIDATED, event.getClaimId(), validatedEvent);
        log.info("Published ClaimValidatedEvent for claim: {}", event.getClaimId());
    }
}
