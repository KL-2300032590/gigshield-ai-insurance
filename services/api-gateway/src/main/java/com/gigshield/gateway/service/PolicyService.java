package com.gigshield.gateway.service;

import com.gigshield.common.dto.PolicyPurchaseRequest;
import com.gigshield.common.dto.PolicyResponse;
import com.gigshield.common.dto.RiskPremiumResponse;
import com.gigshield.common.events.KafkaTopics;
import com.gigshield.common.events.PolicyPurchasedEvent;
import com.gigshield.common.model.Policy;
import com.gigshield.common.utils.DateUtils;
import com.gigshield.gateway.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {
    
    private static final BigDecimal DEFAULT_COVERAGE_LIMIT = new BigDecimal("800.00");
    private static final BigDecimal BASE_PREMIUM = new BigDecimal("20.00");
    
    private final PolicyRepository policyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;
    
    @Transactional
    public PolicyResponse purchasePolicy(PolicyPurchaseRequest request, String workerId) {
        log.info("Processing policy purchase for worker: {}", workerId);
        
        int weekNumber = request.getWeekNumber() > 0 ? request.getWeekNumber() : DateUtils.getCurrentWeekNumber();
        int year = request.getYear() > 0 ? request.getYear() : DateUtils.getCurrentWeekYear();
        
        // Check if policy already exists for this week
        if (policyRepository.existsByWorkerIdAndWeekNumberAndYear(workerId, weekNumber, year)) {
            throw new IllegalArgumentException("Policy already exists for this week");
        }
        
        // Get risk score from Risk Engine (with fallback)
        RiskPremiumResponse riskResponse = getRiskPremium(workerId);
        
        BigDecimal coverageLimit = request.getCoverageLimit() != null ? 
                request.getCoverageLimit() : DEFAULT_COVERAGE_LIMIT;
        
        // Create policy
        Policy policy = Policy.builder()
                .workerId(workerId)
                .weekNumber(weekNumber)
                .year(year)
                .premium(riskResponse != null ? riskResponse.getPremium() : BASE_PREMIUM)
                .coverageLimit(coverageLimit)
                .riskScore(riskResponse != null ? riskResponse.getRiskScore() : 0.5)
                .startDate(DateUtils.getWeekStartDate(weekNumber, year))
                .endDate(DateUtils.getWeekEndDate(weekNumber, year))
                .status(Policy.PolicyStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        policy = policyRepository.save(policy);
        log.info("Policy created with ID: {}", policy.getId());
        
        // Publish event
        publishPolicyPurchasedEvent(policy);
        
        return PolicyResponse.fromPolicy(policy);
    }
    
    public PolicyResponse getActivePolicy(String workerId) {
        int currentWeek = DateUtils.getCurrentWeekNumber();
        int currentYear = DateUtils.getCurrentWeekYear();
        
        Policy policy = policyRepository.findByWorkerIdAndWeekNumberAndYear(workerId, currentWeek, currentYear)
                .orElseThrow(() -> new IllegalArgumentException("No active policy found"));
        
        return PolicyResponse.fromPolicy(policy);
    }
    
    public List<PolicyResponse> getWorkerPolicies(String workerId) {
        return policyRepository.findByWorkerIdOrderByCreatedAtDesc(workerId).stream()
                .map(PolicyResponse::fromPolicy)
                .collect(Collectors.toList());
    }
    
    public PolicyResponse getPolicyById(String policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        return PolicyResponse.fromPolicy(policy);
    }
    
    private RiskPremiumResponse getRiskPremium(String workerId) {
        try {
            String riskEngineUrl = "http://risk-engine:8081/api/risk/premium/" + workerId;
            return restTemplate.getForObject(riskEngineUrl, RiskPremiumResponse.class);
        } catch (Exception e) {
            log.warn("Failed to get risk premium from Risk Engine, using default: {}", e.getMessage());
            return null;
        }
    }
    
    private void publishPolicyPurchasedEvent(Policy policy) {
        PolicyPurchasedEvent event = PolicyPurchasedEvent.builder()
                .eventType(PolicyPurchasedEvent.TYPE)
                .policyId(policy.getId())
                .workerId(policy.getWorkerId())
                .weekNumber(policy.getWeekNumber())
                .year(policy.getYear())
                .premium(policy.getPremium())
                .coverageLimit(policy.getCoverageLimit())
                .riskScore(policy.getRiskScore())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .build();
        
        kafkaTemplate.send(KafkaTopics.POLICY_PURCHASED, policy.getId(), event);
        log.info("Published PolicyPurchasedEvent for policy: {}", policy.getId());
    }
}
