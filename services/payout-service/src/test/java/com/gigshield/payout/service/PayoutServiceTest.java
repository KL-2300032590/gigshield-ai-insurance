package com.gigshield.payout.service;

import com.gigshield.common.events.ClaimApprovedEvent;
import com.gigshield.common.model.Claim;
import com.gigshield.common.model.Payout;
import com.gigshield.common.model.Worker;
import com.gigshield.payout.client.RazorpayClient;
import com.gigshield.payout.repository.ClaimRepository;
import com.gigshield.payout.repository.PayoutRepository;
import com.gigshield.payout.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private WorkerRepository workerRepository;

    @InjectMocks
    private PayoutService payoutService;

    private ClaimApprovedEvent approvedEvent;

    @BeforeEach
    void setUp() {
        approvedEvent = ClaimApprovedEvent.builder()
                .claimId("claim123")
                .policyId("policy123")
                .workerId("worker123")
                .amount(new BigDecimal("800.00"))
                .build();

        ReflectionTestUtils.setField(payoutService, "locationValidationRadiusKm", 50.0);

        Claim claim = Claim.builder()
                .id("claim123")
                .triggerData(Claim.TriggerData.builder()
                        .latitude(19.076)
                        .longitude(72.877)
                        .location("Mumbai")
                        .build())
                .build();
        when(claimRepository.findById("claim123")).thenReturn(Optional.of(claim));

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
    void processApprovedClaim_SuccessfulPayment() {
        RazorpayClient.PayoutResult successResult = RazorpayClient.PayoutResult.builder()
                .success(true)
                .transactionId("txn_12345")
                .message("Payout processed successfully")
                .build();

        when(payoutRepository.existsByClaimId(anyString())).thenReturn(false);
        when(razorpayClient.initiatePayout(anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(Mono.just(successResult));
        when(payoutRepository.save(any())).thenAnswer(i -> {
            Payout payout = i.getArgument(0);
            if (payout.getId() == null) {
                payout.setId("payout123");
            }
            return payout;
        });

        payoutService.processApprovedClaim(approvedEvent);

        // Wait a bit for async processing
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}

        ArgumentCaptor<Payout> captor = ArgumentCaptor.forClass(Payout.class);
        verify(payoutRepository, atLeast(2)).save(captor.capture());

        // Get the last saved payout (after success handling)
        Payout finalPayout = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(finalPayout.getStatus()).isEqualTo(Payout.PayoutStatus.SUCCESS);
        assertThat(finalPayout.getTransactionId()).isEqualTo("txn_12345");

        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void processApprovedClaim_FailedPayment() {
        RazorpayClient.PayoutResult failResult = RazorpayClient.PayoutResult.builder()
                .success(false)
                .transactionId(null)
                .message("Payment gateway temporarily unavailable")
                .build();

        when(payoutRepository.existsByClaimId(anyString())).thenReturn(false);
        when(razorpayClient.initiatePayout(anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(Mono.just(failResult));
        when(payoutRepository.save(any())).thenAnswer(i -> {
            Payout payout = i.getArgument(0);
            if (payout.getId() == null) {
                payout.setId("payout123");
            }
            return payout;
        });

        payoutService.processApprovedClaim(approvedEvent);

        // Wait for async processing and retries
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        verify(payoutRepository, atLeast(1)).save(any());
    }

    @Test
    void processApprovedClaim_SkipsDuplicatePayout() {
        when(payoutRepository.existsByClaimId("claim123")).thenReturn(true);

        payoutService.processApprovedClaim(approvedEvent);

        verify(payoutRepository, never()).save(any());
        verify(razorpayClient, never()).initiatePayout(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    void processApprovedClaim_CreatesCorrectPayoutRecord() {
        RazorpayClient.PayoutResult successResult = RazorpayClient.PayoutResult.builder()
                .success(true)
                .transactionId("txn_12345")
                .message("Payout processed successfully")
                .build();

        when(payoutRepository.existsByClaimId(anyString())).thenReturn(false);
        when(razorpayClient.initiatePayout(anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(Mono.just(successResult));
        when(payoutRepository.save(any())).thenAnswer(i -> {
            Payout payout = i.getArgument(0);
            if (payout.getId() == null) {
                payout.setId("payout123");
            }
            return payout;
        });

        payoutService.processApprovedClaim(approvedEvent);

        ArgumentCaptor<Payout> captor = ArgumentCaptor.forClass(Payout.class);
        verify(payoutRepository, atLeast(1)).save(captor.capture());

        Payout createdPayout = captor.getAllValues().get(0);
        assertThat(createdPayout.getClaimId()).isEqualTo("claim123");
        assertThat(createdPayout.getWorkerId()).isEqualTo("worker123");
        assertThat(createdPayout.getAmount()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(createdPayout.getPaymentMethod()).isEqualTo("UPI");
    }
}
