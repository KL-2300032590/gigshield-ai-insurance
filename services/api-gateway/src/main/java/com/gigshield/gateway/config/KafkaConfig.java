package com.gigshield.gateway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.gigshield.common.events.KafkaTopics.*;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic workerRegisteredTopic() {
        return TopicBuilder.name(WORKER_REGISTERED)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic policyPurchasedTopic() {
        return TopicBuilder.name(POLICY_PURCHASED)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic environmentDisruptionTopic() {
        return TopicBuilder.name(ENVIRONMENT_DISRUPTION)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic claimInitiatedTopic() {
        return TopicBuilder.name(CLAIM_INITIATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic claimValidatedTopic() {
        return TopicBuilder.name(CLAIM_VALIDATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic claimApprovedTopic() {
        return TopicBuilder.name(CLAIM_APPROVED)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic payoutCompletedTopic() {
        return TopicBuilder.name(PAYOUT_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
