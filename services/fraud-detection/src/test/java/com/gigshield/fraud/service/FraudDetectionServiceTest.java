package com.gigshield.fraud.service;

import com.gigshield.common.events.ClaimInitiatedEvent;
import com.gigshield.common.model.Claim.TriggerType;
import com.gigshield.common.model.Worker;
import com.gigshield.fraud.model.FraudCheck;
import com.gigshield.fraud.repository.FraudCheckRepository;
import com.gigshield.fraud.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private FraudCheckRepository fraudCheckRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private WorkerRepository workerRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private ClaimInitiatedEvent claimEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fraudDetectionService, "fraudScoreThreshold", 0.7);
        ReflectionTestUtils.setField(fraudDetectionService, "duplicateClaimHours", 24);
        ReflectionTestUtils.setField(fraudDetectionService, "locationRadiusKm", 50.0);

        claimEvent = ClaimInitiatedEvent.builder()
                .claimId("claim123")
                .policyId("policy123")
                .workerId("worker123")
                .triggerType(TriggerType.HEAVY_RAIN)
                .measuredValue(75.0)
                .threshold(50.0)
                .city("Mumbai")
                .disruptionLatitude(19.076)
                .disruptionLongitude(72.877)
                .amount(new BigDecimal("800"))
                .build();

        Worker worker = Worker.builder()
                .id("worker123")
                .location(Worker.Location.builder()
                        .latitude(19.076)
                        .longitude(72.877)
                        .city("Mumbai")
                        .build())
                .build();
        when(workerRepository.findById("worker123")).thenReturn(Optional.of(worker));
    }

    @Test
    void validateClaim_ApprovedForFirstTimeClaimer() {
        when(fraudCheckRepository.findByWorkerIdAndCheckedAtAfter(any(), any()))
                .thenReturn(Collections.emptyList());
        when(fraudCheckRepository.countByWorkerIdAndCheckedAtAfter(any(), any()))
                .thenReturn(0L);
        when(fraudCheckRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        fraudDetectionService.validateClaim(claimEvent);

        ArgumentCaptor<FraudCheck> captor = ArgumentCaptor.forClass(FraudCheck.class);
        verify(fraudCheckRepository).save(captor.capture());

        FraudCheck savedCheck = captor.getValue();
        assertThat(savedCheck.isApproved()).isTrue();
        assertThat(savedCheck.getFraudScore()).isLessThan(0.7);
        
        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    void validateClaim_RejectedForDuplicateClaim() {
        FraudCheck existingCheck = FraudCheck.builder()
                .claimId("existingClaim")
                .workerId("worker123")
                .build();

        when(fraudCheckRepository.findByWorkerIdAndCheckedAtAfter(any(), any()))
                .thenReturn(Collections.singletonList(existingCheck));
        when(fraudCheckRepository.countByWorkerIdAndCheckedAtAfter(any(), any()))
                .thenReturn(5L); // High frequency
        when(fraudCheckRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        fraudDetectionService.validateClaim(claimEvent);

        ArgumentCaptor<FraudCheck> captor = ArgumentCaptor.forClass(FraudCheck.class);
        verify(fraudCheckRepository).save(captor.capture());

        FraudCheck savedCheck = captor.getValue();
        assertThat(savedCheck.isApproved()).isFalse();
        assertThat(savedCheck.getFraudScore()).isGreaterThanOrEqualTo(0.7);
        assertThat(savedCheck.getFlags()).contains("DUPLICATE_CLAIM");
    }

    @Test
    void validateClaim_FlagsHighFrequencyClaims() {
        when(fraudCheckRepository.findByWorkerIdAndCheckedAtAfter(any(), any()))
                .thenReturn(Collections.emptyList());
        when(fraudCheckRepository.countByWorkerIdAndCheckedAtAfter(any(), any()))
                .thenReturn(5L);
        when(fraudCheckRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        fraudDetectionService.validateClaim(claimEvent);

        ArgumentCaptor<FraudCheck> captor = ArgumentCaptor.forClass(FraudCheck.class);
        verify(fraudCheckRepository).save(captor.capture());

        FraudCheck savedCheck = captor.getValue();
        assertThat(savedCheck.getFlags()).contains("HIGH_CLAIM_FREQUENCY");
    }
}
