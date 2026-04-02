package com.gigshield.common.events;

/**
 * Kafka topic names used across all microservices.
 */
public final class KafkaTopics {
    
    private KafkaTopics() {
        // Utility class
    }
    
    // Worker events
    public static final String WORKER_REGISTERED = "gigshield.worker.registered";
    
    // Policy events
    public static final String POLICY_PURCHASED = "gigshield.policy.purchased";
    
    // Environment events
    public static final String ENVIRONMENT_DISRUPTION = "gigshield.environment.disruption";
    
    // Claim events
    public static final String CLAIM_INITIATED = "gigshield.claim.initiated";
    public static final String CLAIM_VALIDATED = "gigshield.claim.validated";
    public static final String CLAIM_APPROVED = "gigshield.claim.approved";
    public static final String CLAIM_REJECTED = "gigshield.claim.rejected";
    
    // Payout events
    public static final String PAYOUT_COMPLETED = "gigshield.payout.completed";
    
    // Notification events
    public static final String NOTIFICATION_SEND = "gigshield.notification.send";
}
