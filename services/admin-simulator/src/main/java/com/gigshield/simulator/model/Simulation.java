package com.gigshield.simulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Simulation entity - stores history of triggered simulations
 * 
 * Tracks each weather simulation execution including:
 * - Simulation parameters (city, event type, severity)
 * - Execution metadata (status, timestamps, duration)
 * - Results (events published, claims triggered)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "simulations")
public class Simulation {

    @Id
    private String id;

    /**
     * City where simulation was triggered (e.g., "Mumbai", "Delhi")
     */
    private String city;

    /**
     * Type of weather event (e.g., "HEAVY_RAIN", "HIGH_AQI", "EXTREME_HEAT")
     */
    private String eventType;

    /**
     * Severity level: LOW, MEDIUM, HIGH, CRITICAL
     */
    private String severity;

    /**
     * Duration of the event (e.g., "2h", "30m", "1d")
     */
    private String duration;

    /**
     * Simulated value (e.g., rainfall in mm, AQI value, temperature)
     */
    private Double simulatedValue;

    /**
     * Number of workers affected by this simulation
     */
    private Integer affectedWorkers;

    /**
     * Whether to automatically trigger claims for affected workers
     */
    private Boolean triggerClaims;

    /**
     * Execution status: INITIATED, IN_PROGRESS, COMPLETED, FAILED
     */
    private String status;

    /**
     * Number of Kafka events published
     */
    private Integer eventsPublished;

    /**
     * Number of claims triggered as result
     */
    private Integer claimsTriggered;

    /**
     * List of triggered claim IDs for tracking
     */
    private List<String> claimIds;

    /**
     * Error message if simulation failed
     */
    private String errorMessage;

    /**
     * Execution time in milliseconds
     */
    private Long executionTimeMs;

    /**
     * When simulation was initiated
     */
    private Instant createdAt;

    /**
     * When simulation completed
     */
    private Instant completedAt;

    /**
     * User who triggered the simulation
     */
    private String triggeredBy;

    /**
     * Additional metadata (JSON format)
     */
    private String metadata;
}
