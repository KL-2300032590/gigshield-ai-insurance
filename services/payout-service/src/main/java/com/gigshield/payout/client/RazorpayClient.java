package com.gigshield.payout.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

/**
 * Razorpay Payment Gateway Client (Stub Implementation)
 * In production, this would integrate with actual Razorpay API
 */
@Slf4j
@Component
public class RazorpayClient {
    
    private final WebClient webClient;
    private final String keyId;
    private final String keySecret;
    
    public RazorpayClient(
            @Value("${payment.razorpay.base-url}") String baseUrl,
            @Value("${payment.razorpay.key-id}") String keyId,
            @Value("${payment.razorpay.key-secret}") String keySecret) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.keyId = keyId;
        this.keySecret = keySecret;
    }
    
    public Mono<PayoutResult> initiatePayout(String workerId, String upiId, BigDecimal amount) {
        log.info("Initiating payout to {} for amount ₹{}", upiId, amount);
        
        // Stub implementation - simulates Razorpay payout
        if (keyId.startsWith("rzp_test") || "rzp_test_demo".equals(keyId)) {
            return simulatePayout(workerId, upiId, amount);
        }
        
        // Real implementation would call Razorpay API
        return callRazorpayApi(workerId, upiId, amount);
    }
    
    public Mono<PayoutStatus> checkPayoutStatus(String transactionId) {
        log.info("Checking payout status for transaction: {}", transactionId);
        
        // Stub implementation
        if (keyId.startsWith("rzp_test") || "rzp_test_demo".equals(keyId)) {
            return Mono.just(PayoutStatus.builder()
                    .transactionId(transactionId)
                    .status("processed")
                    .build());
        }
        
        return fetchPayoutStatus(transactionId);
    }
    
    private Mono<PayoutResult> simulatePayout(String workerId, String upiId, BigDecimal amount) {
        // Simulate network delay
        return Mono.delay(Duration.ofMillis(500))
                .map(tick -> {
                    // 95% success rate for simulation
                    boolean success = Math.random() < 0.95;
                    
                    if (success) {
                        String transactionId = "txn_" + UUID.randomUUID().toString().substring(0, 16);
                        log.info("Simulated payout successful: {}", transactionId);
                        
                        return PayoutResult.builder()
                                .success(true)
                                .transactionId(transactionId)
                                .message("Payout processed successfully")
                                .build();
                    } else {
                        log.warn("Simulated payout failed for worker: {}", workerId);
                        
                        return PayoutResult.builder()
                                .success(false)
                                .transactionId(null)
                                .message("Payment gateway temporarily unavailable")
                                .build();
                    }
                });
    }
    
    private Mono<PayoutResult> callRazorpayApi(String workerId, String upiId, BigDecimal amount) {
        // Real Razorpay API integration
        return webClient.post()
                .uri("/payouts")
                .bodyValue(CreatePayoutRequest.builder()
                        .accountNumber(workerId)
                        .fundAccountId(upiId)
                        .amount(amount.multiply(BigDecimal.valueOf(100)).intValue()) // Razorpay uses paise
                        .currency("INR")
                        .mode("UPI")
                        .purpose("claim_payout")
                        .build())
                .retrieve()
                .bodyToMono(RazorpayResponse.class)
                .map(response -> PayoutResult.builder()
                        .success("processed".equals(response.getStatus()))
                        .transactionId(response.getId())
                        .message(response.getStatus())
                        .build())
                .onErrorResume(e -> {
                    log.error("Razorpay API error: {}", e.getMessage());
                    return Mono.just(PayoutResult.builder()
                            .success(false)
                            .message("Payment gateway error: " + e.getMessage())
                            .build());
                });
    }
    
    private Mono<PayoutStatus> fetchPayoutStatus(String transactionId) {
        return webClient.get()
                .uri("/payouts/{id}", transactionId)
                .retrieve()
                .bodyToMono(RazorpayResponse.class)
                .map(response -> PayoutStatus.builder()
                        .transactionId(response.getId())
                        .status(response.getStatus())
                        .build())
                .onErrorResume(e -> Mono.just(PayoutStatus.builder()
                        .transactionId(transactionId)
                        .status("unknown")
                        .build()));
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayoutResult {
        private boolean success;
        private String transactionId;
        private String message;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayoutStatus {
        private String transactionId;
        private String status;
    }
    
    @Data
    @Builder
    private static class CreatePayoutRequest {
        private String accountNumber;
        private String fundAccountId;
        private int amount;
        private String currency;
        private String mode;
        private String purpose;
    }
    
    @Data
    private static class RazorpayResponse {
        private String id;
        private String status;
    }
}
