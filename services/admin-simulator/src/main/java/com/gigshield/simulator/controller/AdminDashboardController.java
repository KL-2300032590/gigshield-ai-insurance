package com.gigshield.simulator.controller;

import com.gigshield.simulator.dto.dashboard.AdminClaimDto;
import com.gigshield.simulator.dto.dashboard.AdminKafkaEventDto;
import com.gigshield.simulator.dto.dashboard.AdminLogDto;
import com.gigshield.simulator.dto.dashboard.AdminPolicyDto;
import com.gigshield.simulator.dto.dashboard.AdminServiceHealthDto;
import com.gigshield.simulator.dto.dashboard.AdminWorkerDto;
import com.gigshield.simulator.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/api/admin/health")
    public Mono<List<AdminServiceHealthDto>> getServicesHealth() {
        return adminDashboardService.getServicesHealth();
    }

    @GetMapping("/api/admin/health/{serviceName}")
    public Mono<AdminServiceHealthDto> getServiceHealth(@PathVariable String serviceName) {
        return adminDashboardService.getServiceHealth(serviceName);
    }

    @GetMapping("/api/admin/metrics")
    public Mono<Map<String, Number>> getMetrics() {
        return adminDashboardService.getMetrics();
    }

    @GetMapping("/api/workers")
    public Mono<List<AdminWorkerDto>> getWorkers() {
        return adminDashboardService.getWorkers();
    }

    @GetMapping("/api/workers/{id}")
    public Mono<ResponseEntity<AdminWorkerDto>> getWorkerById(@PathVariable String id) {
        return adminDashboardService.getWorkerById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/policies")
    public Mono<List<AdminPolicyDto>> getPolicies() {
        return adminDashboardService.getPolicies();
    }

    @GetMapping("/api/policies/{id}")
    public Mono<ResponseEntity<AdminPolicyDto>> getPolicyById(@PathVariable String id) {
        return adminDashboardService.getPolicyById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/claims")
    public Mono<List<AdminClaimDto>> getClaims(
            @RequestParam Optional<String> status,
            @RequestParam Optional<String> city) {
        return adminDashboardService.getClaims(status, city);
    }

    @GetMapping("/api/claims/{id}")
    public Mono<ResponseEntity<AdminClaimDto>> getClaimById(@PathVariable String id) {
        return adminDashboardService.getClaimById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/admin/events")
    public Mono<List<AdminKafkaEventDto>> getEvents() {
        return adminDashboardService.getEvents();
    }

    @GetMapping("/api/admin/logs")
    public Mono<List<AdminLogDto>> getLogs() {
        return adminDashboardService.getLogs();
    }

    @GetMapping("/api/admin/events/stream")
    public Mono<List<AdminKafkaEventDto>> getEventsStream() {
        return adminDashboardService.getEvents();
    }
}
