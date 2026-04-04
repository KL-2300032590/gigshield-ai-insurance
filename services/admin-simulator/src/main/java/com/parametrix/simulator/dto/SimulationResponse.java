package com.parametrix.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for simulation execution
 * 
 * Example:
 * {
 *   "simulationId": "sim_123abc",
 *   "status": "COMPLETED",
 *   "city": "Mumbai",
 *   "eventType": "HEAVY_RAIN",
 *   "eventsPublished": 1,
 *   "claimsTriggered": 45,
 *   "claimIds": ["claim_1", "claim_2"],
 *   "executionTime": "1.2s",
 *   "timestamp": "2026-04-02T08:30:00Z",
 *   "message": "Simulation completed successfully"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponse {

    /**
     * Unique simulation ID
     */
    private String simulationId;

    /**
     * Execution status: INITIATED, IN_PROGRESS, COMPLETED, FAILED
     */
    private String status;

    /**
     * City where simulation occurred
     */
    private String city;

    /**
     * Type of weather event
     */
    private String eventType;

    /**
     * Severity level
     */
    private String severity;

    /**
     * Number of Kafka events published
     */
    private Integer eventsPublished;

    /**
     * Number of claims triggered
     */
    private Integer claimsTriggered;

    /**
     * List of claim IDs triggered
     */
    private List<String> claimIds;

    /**
     * Execution time in human-readable format (e.g., "1.2s")
     */
    private String executionTime;

    /**
     * When simulation was triggered
     */
    private Instant timestamp;

    /**
     * Success/error message
     */
    private String message;

    /**
     * Error details if failed
     */
    private String error;
}
