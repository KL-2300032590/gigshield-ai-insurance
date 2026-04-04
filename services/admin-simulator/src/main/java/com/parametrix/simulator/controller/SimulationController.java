package com.parametrix.simulator.controller;

import com.parametrix.simulator.dto.SimulationRequest;
import com.parametrix.simulator.dto.SimulationResponse;
import com.parametrix.simulator.model.Simulation;
import com.parametrix.simulator.service.SimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Admin Simulator REST Controller
 * 
 * Provides endpoints for triggering and managing weather simulations.
 * 
 * Base Path: /api/admin/simulate
 * 
 * Endpoints:
 * - POST /weather - Trigger weather simulation
 * - POST /disruption - Trigger generic disruption (alias for /weather)
 * - GET /simulations - List all simulations
 * - GET /simulations/{id} - Get simulation details
 * - GET /simulations/city/{city} - Get simulations by city
 * - DELETE /simulations/{id} - Delete simulation
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/simulate")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    /**
     * Trigger a weather simulation
     * 
     * Example request:
     * POST /api/admin/simulate/weather
     * {
     *   "city": "Mumbai",
     *   "eventType": "HEAVY_RAIN",
     *   "severity": "HIGH",
     *   "duration": "2h",
     *   "simulatedValue": 75.5,
     *   "triggerClaims": true
     * }
     * 
     * @param request Simulation request
     * @return Simulation response
     */
    @PostMapping("/weather")
    public Mono<ResponseEntity<SimulationResponse>> triggerWeatherSimulation(
            @Valid @RequestBody SimulationRequest request) {
        log.info("Received weather simulation request: {}", request);

        return simulationService.triggerSimulation(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(error -> {
                    log.error("Failed to trigger simulation: {}", error.getMessage());
                    SimulationResponse errorResponse = SimulationResponse.builder()
                            .status("FAILED")
                            .message("Simulation failed")
                            .error(error.getMessage())
                            .build();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse));
                });
    }

    /**
     * Trigger a generic disruption (alias for weather simulation)
     * 
     * @param request Simulation request
     * @return Simulation response
     */
    @PostMapping("/disruption")
    public Mono<ResponseEntity<SimulationResponse>> triggerDisruption(
            @Valid @RequestBody SimulationRequest request) {
        return triggerWeatherSimulation(request);
    }

    /**
     * Get all simulations
     * 
     * GET /api/admin/simulate/simulations
     * 
     * @return List of all simulations
     */
    @GetMapping("/simulations")
    public Flux<Simulation> getAllSimulations() {
        log.info("Fetching all simulations");
        return simulationService.getAllSimulations();
    }

    /**
     * Get simulation by ID
     * 
     * GET /api/admin/simulate/simulations/{id}
     * 
     * @param id Simulation ID
     * @return Simulation details
     */
    @GetMapping("/simulations/{id}")
    public Mono<ResponseEntity<Simulation>> getSimulationById(@PathVariable String id) {
        log.info("Fetching simulation: id={}", id);

        return simulationService.getSimulationById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get simulations by city
     * 
     * GET /api/admin/simulate/simulations/city/{city}
     * 
     * @param city City name
     * @return List of simulations for the city
     */
    @GetMapping("/simulations/city/{city}")
    public Flux<Simulation> getSimulationsByCity(@PathVariable String city) {
        log.info("Fetching simulations for city: {}", city);
        return simulationService.getSimulationsByCity(city);
    }

    /**
     * Delete simulation
     * 
     * DELETE /api/admin/simulate/simulations/{id}
     * 
     * @param id Simulation ID
     * @return No content response
     */
    @DeleteMapping("/simulations/{id}")
    public Mono<ResponseEntity<Void>> deleteSimulation(@PathVariable String id) {
        log.info("Deleting simulation: id={}", id);

        return simulationService.deleteSimulation(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> {
                    log.error("Failed to delete simulation: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Health check endpoint
     * 
     * GET /api/admin/simulate/health
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("Admin Simulator Service is running"));
    }
}
