package com.gigshield.simulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a weather simulation
 * 
 * Example:
 * {
 *   "city": "Mumbai",
 *   "eventType": "HEAVY_RAIN",
 *   "severity": "HIGH",
 *   "duration": "2h",
 *   "simulatedValue": 75.5,
 *   "affectedWorkers": 150,
 *   "triggerClaims": true
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {

    /**
     * City where simulation should occur
     * Valid values: Mumbai, Delhi, Bangalore, Hyderabad, Chennai, Kolkata, Pune
     */
    @NotBlank(message = "City is required")
    private String city;

    /**
     * Type of weather event to simulate
     * Valid values: HEAVY_RAIN, FLOOD, HIGH_AQI, EXTREME_HEAT, EXTREME_COLD
     */
    @NotBlank(message = "Event type is required")
    private String eventType;

    /**
     * Severity of the event
     * Valid values: LOW, MEDIUM, HIGH, CRITICAL
     */
    @NotBlank(message = "Severity is required")
    private String severity;

    /**
     * Duration of the event
     * Format: number + unit (e.g., "2h", "30m", "1d")
     */
    @NotBlank(message = "Duration is required")
    private String duration;

    /**
     * Simulated value for the measurement
     * - For HEAVY_RAIN/FLOOD: rainfall in mm
     * - For HIGH_AQI: AQI value (0-500)
     * - For EXTREME_HEAT/COLD: temperature in Celsius
     */
    @NotNull(message = "Simulated value is required")
    @Positive(message = "Simulated value must be positive")
    private Double simulatedValue;

    /**
     * Number of workers to be affected
     * Default: System will calculate based on city and event type
     */
    private Integer affectedWorkers;

    /**
     * Whether to automatically trigger claims
     * Default: true
     */
    @Builder.Default
    private Boolean triggerClaims = true;

    /**
     * User triggering the simulation (for audit)
     */
    private String triggeredBy;
}
