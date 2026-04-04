package com.parametrix.common.events;

/**
 * Kafka topic names used across all microservices.
 */
public final class KafkaTopics {
    
    private KafkaTopics() {
        // Utility class
    }
    
    // Worker events
    public static final String WORKER_REGISTERED = "parametrix.worker.registered";
    
    // Policy events
    public static final String POLICY_PURCHASED = "parametrix.policy.purchased";
    
    // Environment events
    public static final String ENVIRONMENT_DISRUPTION = "parametrix.environment.disruption";
    
    // Claim events
    public static final String CLAIM_INITIATED = "parametrix.claim.initiated";
    public static final String CLAIM_VALIDATED = "parametrix.claim.validated";
    public static final String CLAIM_APPROVED = "parametrix.claim.approved";
    public static final String CLAIM_REJECTED = "parametrix.claim.rejected";
    
    // Payout events
    public static final String PAYOUT_COMPLETED = "parametrix.payout.completed";
    
    // Notification events
    public static final String NOTIFICATION_SEND = "parametrix.notification.send";
}
