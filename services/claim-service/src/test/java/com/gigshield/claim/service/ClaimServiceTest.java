package com.gigshield.claim.service;

import com.gigshield.claim.repository.ClaimRepository;
import com.gigshield.claim.repository.PolicyRepository;
import com.gigshield.common.events.EnvironmentDisruptionEvent;
import com.gigshield.common.model.Claim;
import com.gigshield.common.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ClaimService claimService;

    private EnvironmentDisruptionEvent disruptionEvent;
    private Policy activePolicy;

    @BeforeEach
    void setUp() {
        disruptionEvent = EnvironmentDisruptionEvent.builder()
                .eventId("event123")
                .disruptionType(Claim.TriggerType.HEAVY_RAIN)
                .city("Mumbai")
                .latitude(19.076)
                .longitude(72.877)
                .measuredValue(75.0)
                .threshold(50.0)
                .severity(EnvironmentDisruptionEvent.Severity.HIGH)
                .build();

        activePolicy = Policy.builder()
                .id("policy123")
                .workerId("worker123")
                .premium(new BigDecimal("35.00"))
                .coverageLimit(new BigDecimal("800.00"))
                .status(Policy.PolicyStatus.ACTIVE)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(6, ChronoUnit.DAYS))
                .city("Mumbai")
                .build();
    }

    @Test
    void processDisruption_CreatesClaimForAffectedPolicy() {
        when(policyRepository.findByStatusAndCityAndEndDateAfter(
                Policy.PolicyStatus.ACTIVE, "Mumbai", any()))
                .thenReturn(List.of(activePolicy));
        when(claimRepository.findByPolicyIdAndTriggeredAtAfter(any(), any()))
                .thenReturn(Collections.emptyList());
        when(claimRepository.save(any())).thenAnswer(i -> {
            Claim claim = i.getArgument(0);
            claim.setId("claim123");
            return claim;
        });

        claimService.processDisruption(disruptionEvent);

        ArgumentCaptor<Claim> captor = ArgumentCaptor.forClass(Claim.class);
        verify(claimRepository).save(captor.capture());

        Claim savedClaim = captor.getValue();
        assertThat(savedClaim.getPolicyId()).isEqualTo("policy123");
        assertThat(savedClaim.getWorkerId()).isEqualTo("worker123");
        assertThat(savedClaim.getTriggerType()).isEqualTo(Claim.TriggerType.HEAVY_RAIN);
        assertThat(savedClaim.getAmount()).isEqualTo(new BigDecimal("800.00"));
        assertThat(savedClaim.getStatus()).isEqualTo(Claim.ClaimStatus.PENDING);

        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    void processDisruption_SkipsDuplicateClaimForSameEvent() {
        Claim existingClaim = Claim.builder()
                .id("existingClaim")
                .policyId("policy123")
                .triggerType(Claim.TriggerType.HEAVY_RAIN)
                .build();

        when(policyRepository.findByStatusAndCityAndEndDateAfter(
                Policy.PolicyStatus.ACTIVE, "Mumbai", any()))
                .thenReturn(List.of(activePolicy));
        when(claimRepository.findByPolicyIdAndTriggeredAtAfter(any(), any()))
                .thenReturn(List.of(existingClaim));

        claimService.processDisruption(disruptionEvent);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void processDisruption_NoClaimsForNoPolicies() {
        when(policyRepository.findByStatusAndCityAndEndDateAfter(
                Policy.PolicyStatus.ACTIVE, "Mumbai", any()))
                .thenReturn(Collections.emptyList());

        claimService.processDisruption(disruptionEvent);

        verify(claimRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
