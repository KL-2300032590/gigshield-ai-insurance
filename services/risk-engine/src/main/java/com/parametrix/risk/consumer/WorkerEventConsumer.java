package com.parametrix.risk.consumer;

import com.parametrix.common.events.KafkaTopics;
import com.parametrix.common.events.WorkerRegisteredEvent;
import com.parametrix.risk.service.RiskCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerEventConsumer {
    
    private final RiskCalculationService riskCalculationService;
    
    @KafkaListener(topics = KafkaTopics.WORKER_REGISTERED, groupId = "risk-engine")
    public void handleWorkerRegistered(WorkerRegisteredEvent event) {
        log.info("Received WorkerRegisteredEvent for worker: {}", event.getWorkerId());
        
        try {
            // Pre-calculate risk score for new worker
            riskCalculationService.calculateRiskPremium(
                    event.getWorkerId(),
                    event.getCity(),
                    event.getLatitude(),
                    event.getLongitude()
            );
            
            log.info("Pre-calculated risk score for new worker: {}", event.getWorkerId());
        } catch (Exception e) {
            log.error("Failed to calculate risk score for worker {}: {}", 
                    event.getWorkerId(), e.getMessage());
        }
    }
}
