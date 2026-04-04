package com.parametrix.simulator.service;

import com.parametrix.common.events.EnvironmentDisruptionEvent;
import com.parametrix.common.model.Claim.TriggerType;
import com.parametrix.simulator.dto.SimulationRequest;
import com.parametrix.simulator.dto.SimulationResponse;
import com.parametrix.simulator.model.Simulation;
import com.parametrix.simulator.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

/**
 * Simulation Service - handles weather simulation logic
 * 
 * Responsibilities:
 * - Create and execute simulations
 * - Publish synthetic events to Kafka
 * - Track simulation history
 * - Calculate affected workers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Kafka topic for environment disruptions
    private static final String ENVIRONMENT_TOPIC = "environment-disruptions";

    // Event type mappings for trigger-engine (maps request type to TriggerType enum)
    private static final Map<String, TriggerType> EVENT_TYPE_MAP = Map.of(
            "HEAVY_RAIN", TriggerType.HEAVY_RAIN,
            "FLOOD", TriggerType.FLOOD,
            "HIGH_AQI", TriggerType.AIR_POLLUTION,
            "AIR_POLLUTION", TriggerType.AIR_POLLUTION,
            "EXTREME_HEAT", TriggerType.EXTREME_HEAT,
            "EXTREME_COLD", TriggerType.EXTREME_COLD
    );

    // City coordinates for geolocation
    private static final Map<String, double[]> CITY_COORDINATES = Map.of(
            "Mumbai", new double[]{19.0760, 72.8777},
            "Delhi", new double[]{28.7041, 77.1025},
            "Bangalore", new double[]{12.9716, 77.5946},
            "Hyderabad", new double[]{17.3850, 78.4867},
            "Chennai", new double[]{13.0827, 80.2707},
            "Kolkata", new double[]{22.5726, 88.3639},
            "Pune", new double[]{18.5204, 73.8567}
    );

    // Default affected workers by city (approximation)
    private static final Map<String, Integer> CITY_WORKER_COUNT = Map.of(
            "Mumbai", 5000,
            "Delhi", 4500,
            "Bangalore", 4000,
            "Hyderabad", 3500,
            "Chennai", 3000,
            "Kolkata", 2500,
            "Pune", 2000
    );

    /**
     * Trigger a new weather simulation
     *
     * @param request Simulation parameters
     * @return Mono of simulation response
     */
    public Mono<SimulationResponse> triggerSimulation(SimulationRequest request) {
        log.info("Triggering simulation: city={}, eventType={}, severity={}",
                request.getCity(), request.getEventType(), request.getSeverity());

        Instant startTime = Instant.now();

        // Create simulation entity
        Simulation simulation = Simulation.builder()
                .city(request.getCity())
                .eventType(request.getEventType())
                .severity(request.getSeverity())
                .duration(request.getDuration())
                .simulatedValue(request.getSimulatedValue())
                .affectedWorkers(calculateAffectedWorkers(request))
                .triggerClaims(request.getTriggerClaims())
                .status("INITIATED")
                .eventsPublished(0)
                .claimsTriggered(0)
                .claimIds(new ArrayList<>())
                .createdAt(Instant.now())
                .triggeredBy(request.getTriggeredBy() != null ? request.getTriggeredBy() : "admin")
                .build();

        return simulationRepository.save(simulation)
                .flatMap(savedSimulation -> {
                    // Update status to in progress
                    savedSimulation.setStatus("IN_PROGRESS");

                    // Publish event to Kafka
                    return publishEventToKafka(savedSimulation)
                            .flatMap(publishedCount -> {
                                // Update simulation with results
                                savedSimulation.setEventsPublished(publishedCount);
                                savedSimulation.setStatus("COMPLETED");
                                savedSimulation.setCompletedAt(Instant.now());
                                savedSimulation.setExecutionTimeMs(
                                        Instant.now().toEpochMilli() - startTime.toEpochMilli());

                                return simulationRepository.save(savedSimulation);
                            })
                            .map(this::mapToResponse)
                            .onErrorResume(error -> {
                                // Handle failure
                                log.error("Simulation failed: {}", error.getMessage(), error);
                                savedSimulation.setStatus("FAILED");
                                savedSimulation.setErrorMessage(error.getMessage());
                                savedSimulation.setCompletedAt(Instant.now());

                                return simulationRepository.save(savedSimulation)
                                        .map(this::mapToResponse);
                            });
                });
    }

    /**
     * Publish simulation event to Kafka
     *
     * @param simulation Simulation entity
     * @return Mono of event count published
     */
    private Mono<Integer> publishEventToKafka(Simulation simulation) {
        try {
            // Get city coordinates
            double[] coords = CITY_COORDINATES.getOrDefault(simulation.getCity(), new double[]{0.0, 0.0});

            // Map severity string to enum
            EnvironmentDisruptionEvent.Severity severity = mapSeverity(simulation.getSeverity());

            // Create environment disruption event matching the common model
            EnvironmentDisruptionEvent event = EnvironmentDisruptionEvent.builder()
                    .eventId("sim_" + simulation.getId())
                    .city(simulation.getCity())
                    .triggerType(getMappedEventType(simulation.getEventType()))
                    .latitude(coords[0])
                    .longitude(coords[1])
                    .measuredValue(simulation.getSimulatedValue())
                    .threshold(getThresholdForEventType(simulation.getEventType()))
                    .source("SIMULATION")
                    .measuredAt(Instant.now())
                    .severity(severity)
                    .timestamp(Instant.now())
                    .build();

            // Publish to Kafka
            kafkaTemplate.send(ENVIRONMENT_TOPIC, event.getCity(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish simulation event: {}", ex.getMessage());
                        } else {
                            log.info("Published simulation event: id={}, city={}, type={}",
                                    simulation.getId(), simulation.getCity(), simulation.getEventType());
                        }
                    });

            return Mono.just(1);  // One event published
        } catch (Exception e) {
            log.error("Error publishing event to Kafka: {}", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    /**
     * Map severity string to enum
     */
    private EnvironmentDisruptionEvent.Severity mapSeverity(String severity) {
        return switch (severity.toUpperCase()) {
            case "LOW" -> EnvironmentDisruptionEvent.Severity.LOW;
            case "MEDIUM" -> EnvironmentDisruptionEvent.Severity.MEDIUM;
            case "HIGH" -> EnvironmentDisruptionEvent.Severity.HIGH;
            case "CRITICAL" -> EnvironmentDisruptionEvent.Severity.CRITICAL;
            default -> EnvironmentDisruptionEvent.Severity.MEDIUM;
        };
    }

    /**
     * Get threshold value for event type
     */
    private double getThresholdForEventType(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "HEAVY_RAIN", "FLOOD" -> 50.0;  // mm rainfall
            case "HIGH_AQI", "AIR_POLLUTION" -> 200.0;  // AQI value
            case "EXTREME_HEAT" -> 40.0;  // Celsius
            case "EXTREME_COLD" -> 5.0;   // Celsius
            default -> 100.0;
        };
    }

    /**
     * Get all simulations
     *
     * @return Flux of all simulations
     */
    public Flux<Simulation> getAllSimulations() {
        return simulationRepository.findAll();
    }

    /**
     * Get simulation by ID
     *
     * @param id Simulation ID
     * @return Mono of simulation
     */
    public Mono<Simulation> getSimulationById(String id) {
        return simulationRepository.findById(id);
    }

    /**
     * Get simulations by city
     *
     * @param city City name
     * @return Flux of simulations
     */
    public Flux<Simulation> getSimulationsByCity(String city) {
        return simulationRepository.findByCity(city);
    }

    /**
     * Delete simulation by ID
     *
     * @param id Simulation ID
     * @return Mono of void
     */
    public Mono<Void> deleteSimulation(String id) {
        return simulationRepository.deleteById(id);
    }

    /**
     * Calculate affected workers based on request
     *
     * @param request Simulation request
     * @return Number of affected workers
     */
    private Integer calculateAffectedWorkers(SimulationRequest request) {
        if (request.getAffectedWorkers() != null) {
            return request.getAffectedWorkers();
        }

        // Default calculation based on city and severity
        int cityWorkers = CITY_WORKER_COUNT.getOrDefault(request.getCity(), 1000);
        double severityFactor = switch (request.getSeverity().toUpperCase()) {
            case "LOW" -> 0.05;
            case "MEDIUM" -> 0.15;
            case "HIGH" -> 0.30;
            case "CRITICAL" -> 0.50;
            default -> 0.10;
        };

        return (int) (cityWorkers * severityFactor);
    }

    /**
     * Map event type to TriggerType enum
     *
     * @param eventType Event type from request
     * @return TriggerType enum value
     */
    private TriggerType getMappedEventType(String eventType) {
        return EVENT_TYPE_MAP.getOrDefault(eventType.toUpperCase(), TriggerType.HEAVY_RAIN);
    }

    /**
     * Map Simulation entity to Response DTO
     *
     * @param simulation Simulation entity
     * @return SimulationResponse DTO
     */
    private SimulationResponse mapToResponse(Simulation simulation) {
        String executionTime = simulation.getExecutionTimeMs() != null
                ? String.format("%.2fs", simulation.getExecutionTimeMs() / 1000.0)
                : "N/A";

        String message = "COMPLETED".equals(simulation.getStatus())
                ? "Simulation completed successfully"
                : simulation.getErrorMessage();

        return SimulationResponse.builder()
                .simulationId(simulation.getId())
                .status(simulation.getStatus())
                .city(simulation.getCity())
                .eventType(simulation.getEventType())
                .severity(simulation.getSeverity())
                .eventsPublished(simulation.getEventsPublished())
                .claimsTriggered(simulation.getClaimsTriggered())
                .claimIds(simulation.getClaimIds())
                .executionTime(executionTime)
                .timestamp(simulation.getCreatedAt())
                .message(message)
                .error(simulation.getErrorMessage())
                .build();
    }
}
