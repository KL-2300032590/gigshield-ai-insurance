package com.gigshield.simulator.service;

import com.gigshield.simulator.dto.SimulationRequest;
import com.gigshield.simulator.dto.SimulationResponse;
import com.gigshield.simulator.model.Simulation;
import com.gigshield.simulator.repository.SimulationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SimulationService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimulationServiceTest {

    @Mock
    private SimulationRepository simulationRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private SimulationService simulationService;

    @BeforeEach
    void setUp() {
        simulationService = new SimulationService(simulationRepository, kafkaTemplate);
        // Default stub for Kafka to avoid NPE
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void triggerSimulation_Success() {
        // Given
        SimulationRequest request = SimulationRequest.builder()
                .city("Mumbai")
                .eventType("HEAVY_RAIN")
                .severity("HIGH")
                .duration("2h")
                .simulatedValue(75.5)
                .triggerClaims(true)
                .build();

        Simulation savedSimulation = Simulation.builder()
                .id("sim_123")
                .city("Mumbai")
                .eventType("HEAVY_RAIN")
                .severity("HIGH")
                .status("INITIATED")
                .build();

        when(simulationRepository.save(any(Simulation.class)))
                .thenReturn(Mono.just(savedSimulation));

        // When & Then
        StepVerifier.create(simulationService.triggerSimulation(request))
                .expectNextMatches(response -> {
                    return response.getCity().equals("Mumbai") &&
                           response.getEventType().equals("HEAVY_RAIN");
                })
                .verifyComplete();
    }

    @Test
    void triggerSimulation_DefaultAffectedWorkers() {
        // Given - request without affectedWorkers
        SimulationRequest request = SimulationRequest.builder()
                .city("Delhi")
                .eventType("HIGH_AQI")
                .severity("MEDIUM")
                .duration("1h")
                .simulatedValue(250.0)
                .build();

        Simulation savedSimulation = Simulation.builder()
                .id("sim_456")
                .city("Delhi")
                .eventType("HIGH_AQI")
                .severity("MEDIUM")
                .affectedWorkers(675)
                .status("COMPLETED")
                .build();

        when(simulationRepository.save(any(Simulation.class)))
                .thenReturn(Mono.just(savedSimulation));

        // When & Then
        StepVerifier.create(simulationService.triggerSimulation(request))
                .expectNextMatches(response -> response.getCity().equals("Delhi"))
                .verifyComplete();
    }

    @Test
    void triggerSimulation_CriticalSeverity() {
        // Given
        SimulationRequest request = SimulationRequest.builder()
                .city("Chennai")
                .eventType("FLOOD")
                .severity("CRITICAL")
                .duration("4h")
                .simulatedValue(100.0)
                .build();

        Simulation savedSimulation = Simulation.builder()
                .id("sim_789")
                .city("Chennai")
                .eventType("FLOOD")
                .severity("CRITICAL")
                .status("COMPLETED")
                .build();

        when(simulationRepository.save(any(Simulation.class)))
                .thenReturn(Mono.just(savedSimulation));

        // When & Then
        StepVerifier.create(simulationService.triggerSimulation(request))
                .expectNextMatches(response -> 
                    response.getSeverity().equals("CRITICAL"))
                .verifyComplete();
    }

    @Test
    void getAllSimulations_Success() {
        // Given
        Simulation sim1 = Simulation.builder().id("sim_1").city("Mumbai").build();
        Simulation sim2 = Simulation.builder().id("sim_2").city("Delhi").build();

        when(simulationRepository.findAll())
                .thenReturn(Flux.just(sim1, sim2));

        // When & Then
        StepVerifier.create(simulationService.getAllSimulations())
                .expectNext(sim1)
                .expectNext(sim2)
                .verifyComplete();
    }

    @Test
    void getSimulationById_Found() {
        // Given
        Simulation simulation = Simulation.builder()
                .id("sim_123")
                .city("Mumbai")
                .eventType("HEAVY_RAIN")
                .build();

        when(simulationRepository.findById("sim_123"))
                .thenReturn(Mono.just(simulation));

        // When & Then
        StepVerifier.create(simulationService.getSimulationById("sim_123"))
                .expectNextMatches(s -> s.getId().equals("sim_123"))
                .verifyComplete();
    }

    @Test
    void getSimulationById_NotFound() {
        // Given
        when(simulationRepository.findById("nonexistent"))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(simulationService.getSimulationById("nonexistent"))
                .verifyComplete();
    }

    @Test
    void getSimulationsByCity_Success() {
        // Given
        Simulation sim1 = Simulation.builder().id("sim_1").city("Mumbai").build();
        Simulation sim2 = Simulation.builder().id("sim_2").city("Mumbai").build();

        when(simulationRepository.findByCity("Mumbai"))
                .thenReturn(Flux.just(sim1, sim2));

        // When & Then
        StepVerifier.create(simulationService.getSimulationsByCity("Mumbai"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void deleteSimulation_Success() {
        // Given
        when(simulationRepository.deleteById("sim_123"))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(simulationService.deleteSimulation("sim_123"))
                .verifyComplete();
    }
}

