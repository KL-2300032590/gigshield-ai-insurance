package com.gigshield.payout.service;

import com.gigshield.common.events.ClaimApprovedEvent;
import com.gigshield.common.model.Payout;
import com.gigshield.payout.client.RazorpayClient;
import com.gigshield.payout.repository.PayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

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
    }

    @Test
    void processPayout_SuccessfulPayment() {
        RazorpayClient.PayoutResult successResult = RazorpayClient.PayoutResult.builder()
                .success(true)
                .transactionId("txn_12345")
                .status("SUCCESS")
                .build();

        when(razorpayClient.initiatePayout(any(), any())).thenReturn(successResult);
        when(payoutRepository.save(any())).thenAnswer(i -> {
            Payout payout = i.getArgument(0);
            payout.setId("payout123");
            return payout;
        });

        payoutService.processPayout(approvedEvent);

        ArgumentCaptor<Payout> captor = ArgumentCaptor.forClass(Payout.class);
        verify(payoutRepository, times(2)).save(captor.capture());

        Payout finalPayout = captor.getAllValues().get(1);
        assertThat(finalPayout.getStatus()).isEqualTo(Payout.PayoutStatus.SUCCESS);
        assertThat(finalPayout.getTransactionId()).isEqualTo("txn_12345");

        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    void processPayout_FailedPayment() {
        RazorpayClient.PayoutResult failResult = RazorpayClient.PayoutResult.builder()
                .success(false)
                .status("FAILED")
                .errorMessage("Insufficient funds in source account")
                .build();

        when(razorpayClient.initiatePayout(any(), any())).thenReturn(failResult);
        when(payoutRepository.save(any())).thenAnswer(i -> {
            Payout payout = i.getArgument(0);
            payout.setId("payout123");
            return payout;
        });

        payoutService.processPayout(approvedEvent);

        ArgumentCaptor<Payout> captor = ArgumentCaptor.forClass(Payout.class);
        verify(payoutRepository, times(2)).save(captor.capture());

        Payout finalPayout = captor.getAllValues().get(1);
        assertThat(finalPayout.getStatus()).isEqualTo(Payout.PayoutStatus.FAILED);
        assertThat(finalPayout.getErrorMessage()).isEqualTo("Insufficient funds in source account");

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void processPayout_InitiatesPayoutCorrectly() {
        RazorpayClient.PayoutResult successResult = RazorpayClient.PayoutResult.builder()
                .success(true)
                .transactionId("txn_12345")
                .status("SUCCESS")
                .build();

        when(razorpayClient.initiatePayout(any(), any())).thenReturn(successResult);
        when(payoutRepository.save(any())).thenAnswer(i -> {
            Payout payout = i.getArgument(0);
            payout.setId("payout123");
            return payout;
        });

        payoutService.processPayout(approvedEvent);

        verify(razorpayClient).initiatePayout("worker123", new BigDecimal("800.00"));
    }
}
