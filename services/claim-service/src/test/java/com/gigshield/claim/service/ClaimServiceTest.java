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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
                .triggerType(Claim.TriggerType.HEAVY_RAIN)
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
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(6))
                .build();
    }

    @Test
    void processDisruption_CreatesClaimForAffectedPolicy() {
        when(policyRepository.findActivePoliciesInCity(
                eq("Mumbai"), any(LocalDate.class), eq(Policy.PolicyStatus.ACTIVE)))
                .thenReturn(List.of(activePolicy));
        when(claimRepository.existsByPolicyIdAndTriggerType(
                eq("policy123"), eq(Claim.TriggerType.HEAVY_RAIN)))
                .thenReturn(false);
        when(claimRepository.save(any())).thenAnswer(i -> {
            Claim claim = i.getArgument(0);
            claim.setId("claim123");
            return claim;
        });

        claimService.processDisruption(disruptionEvent);

        ArgumentCaptor<Claim> captor = ArgumentCaptor.forClass(Claim.class);
        verify(claimRepository, atLeast(1)).save(captor.capture());

        Claim savedClaim = captor.getAllValues().get(0);
        assertThat(savedClaim.getPolicyId()).isEqualTo("policy123");
        assertThat(savedClaim.getWorkerId()).isEqualTo("worker123");
        assertThat(savedClaim.getTriggerType()).isEqualTo(Claim.TriggerType.HEAVY_RAIN);
        assertThat(savedClaim.getAmount()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(savedClaim.getStatus()).isIn(Claim.ClaimStatus.PENDING, Claim.ClaimStatus.VALIDATING);

        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    void processDisruption_SkipsDuplicateClaimForSameEvent() {
        when(policyRepository.findActivePoliciesInCity(
                eq("Mumbai"), any(LocalDate.class), eq(Policy.PolicyStatus.ACTIVE)))
                .thenReturn(List.of(activePolicy));
        when(claimRepository.existsByPolicyIdAndTriggerType(
                eq("policy123"), eq(Claim.TriggerType.HEAVY_RAIN)))
                .thenReturn(true);

        claimService.processDisruption(disruptionEvent);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void processDisruption_NoClaimsForNoPolicies() {
        when(policyRepository.findActivePoliciesInCity(
                eq("Mumbai"), any(LocalDate.class), eq(Policy.PolicyStatus.ACTIVE)))
                .thenReturn(Collections.emptyList());

        claimService.processDisruption(disruptionEvent);

        verify(claimRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
