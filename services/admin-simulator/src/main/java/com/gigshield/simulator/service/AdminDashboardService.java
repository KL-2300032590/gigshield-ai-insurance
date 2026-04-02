package com.gigshield.simulator.service;

import com.gigshield.common.model.Claim;
import com.gigshield.common.model.Policy;
import com.gigshield.common.model.Worker;
import com.gigshield.simulator.dto.dashboard.AdminClaimDto;
import com.gigshield.simulator.dto.dashboard.AdminKafkaEventDto;
import com.gigshield.simulator.dto.dashboard.AdminLogDto;
import com.gigshield.simulator.dto.dashboard.AdminPolicyDto;
import com.gigshield.simulator.dto.dashboard.AdminServiceHealthDto;
import com.gigshield.simulator.dto.dashboard.AdminWorkerDto;
import com.gigshield.simulator.model.Simulation;
import com.gigshield.simulator.repository.ClaimRepository;
import com.gigshield.simulator.repository.PolicyRepository;
import com.gigshield.simulator.repository.SimulationRepository;
import com.gigshield.simulator.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final SimulationRepository simulationRepository;
    private final WorkerRepository workerRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    @Value("${services.health.api-gateway-url:http://api-gateway:8080}")
    private String apiGatewayUrl;

    @Value("${services.health.risk-engine-url:http://risk-engine:8081}")
    private String riskEngineUrl;

    @Value("${services.health.trigger-engine-url:http://trigger-engine:8082}")
    private String triggerEngineUrl;

    @Value("${services.health.fraud-detection-url:http://fraud-detection:8083}")
    private String fraudDetectionUrl;

    @Value("${services.health.claim-service-url:http://claim-service:8084}")
    private String claimServiceUrl;

    @Value("${services.health.payout-service-url:http://payout-service:8085}")
    private String payoutServiceUrl;

    @Value("${services.health.admin-simulator-url:http://admin-simulator:8091}")
    private String adminSimulatorUrl;

    public Mono<List<AdminServiceHealthDto>> getServicesHealth() {
        return Mono.zip(
                serviceHealth("API Gateway", 8080, apiGatewayUrl),
                serviceHealth("Risk Engine", 8081, riskEngineUrl),
                serviceHealth("Trigger Engine", 8082, triggerEngineUrl),
                serviceHealth("Fraud Detection", 8083, fraudDetectionUrl),
                serviceHealth("Claim Service", 8084, claimServiceUrl),
                serviceHealth("Payout Service", 8085, payoutServiceUrl),
                serviceHealth("Admin Simulator", 8091, adminSimulatorUrl)
        ).map(tuple -> List.of(
                tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(),
                tuple.getT5(), tuple.getT6(), tuple.getT7()
        ));
    }

    public Mono<AdminServiceHealthDto> getServiceHealth(String serviceName) {
        return getServicesHealth()
                .flatMap(services -> services.stream()
                        .filter(service -> service.getName().equalsIgnoreCase(serviceName.replace('-', ' ')))
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(() -> Mono.just(AdminServiceHealthDto.builder()
                                .name(serviceName)
                                .status("UNKNOWN")
                                .port(0)
                                .responseTime(0L)
                                .details(Map.of())
                                .build())));
    }

    public Mono<List<AdminWorkerDto>> getWorkers() {
        return workerRepository.findAll()
                .map(this::toAdminWorker)
                .sort(Comparator.comparing(AdminWorkerDto::getRegisteredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collectList();
    }

    public Mono<AdminWorkerDto> getWorkerById(String id) {
        return getWorkers()
                .flatMap(workers -> workers.stream()
                        .filter(worker -> worker.getId().equals(id))
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(Mono::empty));
    }

    public Mono<List<AdminPolicyDto>> getPolicies() {
        return policyRepository.findAll()
                .map(this::toAdminPolicy)
                .sort(Comparator.comparing(AdminPolicyDto::getStartDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collectList();
    }

    public Mono<AdminPolicyDto> getPolicyById(String id) {
        return getPolicies()
                .flatMap(policies -> policies.stream()
                        .filter(policy -> policy.getId().equals(id))
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(Mono::empty));
    }

    public Mono<List<AdminClaimDto>> getClaims(Optional<String> status, Optional<String> city) {
        return claimRepository.findAll()
                .map(this::toAdminClaim)
                .filter(claim -> status
                        .map(s -> claim.getStatus() != null && claim.getStatus().equalsIgnoreCase(s))
                        .orElse(true))
                .filter(claim -> city
                        .map(c -> claim.getCity() != null && claim.getCity().equalsIgnoreCase(c))
                        .orElse(true))
                .sort(Comparator.comparing(AdminClaimDto::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collectList();
    }

    public Mono<AdminClaimDto> getClaimById(String id) {
        return getClaims(Optional.empty(), Optional.empty())
                .flatMap(claims -> claims.stream()
                        .filter(claim -> claim.getId().equals(id))
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(Mono::empty));
    }

    public Mono<List<AdminKafkaEventDto>> getEvents() {
        return simulationRepository.findAll()
                .sort(Comparator.comparing(Simulation::getCreatedAt).reversed())
                .map(this::toEvent)
                .take(100)
                .collectList();
    }

    public Mono<List<AdminLogDto>> getLogs() {
        return simulationRepository.findAll().collectList().map(simulations -> {
            List<AdminLogDto> logs = new ArrayList<>();

            simulations.stream()
                    .sorted(Comparator.comparing(Simulation::getCreatedAt).reversed())
                    .limit(100)
                    .forEach(simulation -> {
                        Instant time = defaultInstant(simulation.getCreatedAt());
                        logs.add(AdminLogDto.builder()
                                .id("log-" + simulation.getId() + "-info")
                                .level("INFO")
                                .service("admin-simulator")
                                .message(String.format("Simulation executed: city=%s, type=%s, severity=%s, status=%s", simulation.getCity(), simulation.getEventType(), simulation.getSeverity(), simulation.getStatus()))
                                .timestamp(time)
                                .build());

                        if (simulation.getErrorMessage() != null && !simulation.getErrorMessage().isBlank()) {
                            logs.add(AdminLogDto.builder()
                                    .id("log-" + simulation.getId() + "-error")
                                    .level("ERROR")
                                    .service("admin-simulator")
                                    .message(simulation.getErrorMessage())
                                    .timestamp(defaultInstant(simulation.getCompletedAt()))
                                    .build());
                        }
                    });

            if (logs.isEmpty()) {
                logs.add(AdminLogDto.builder()
                        .id("log-bootstrap")
                        .level("INFO")
                        .service("admin-simulator")
                        .message("Admin dashboard connected. Trigger simulations to generate live activity.")
                        .timestamp(Instant.now())
                        .build());
            }

            return logs.stream()
                    .sorted(Comparator.comparing(AdminLogDto::getTimestamp).reversed())
                    .toList();
        });
    }

    public Mono<Map<String, Number>> getMetrics() {
        Mono<List<AdminWorkerDto>> workers = getWorkers();
        Mono<List<AdminPolicyDto>> policies = getPolicies();
        Mono<List<AdminClaimDto>> claims = getClaims(Optional.empty(), Optional.empty());
        Mono<List<AdminKafkaEventDto>> events = getEvents();

        return Mono.zip(workers, policies, claims, events)
                .map(tuple -> {
                    List<AdminWorkerDto> workerList = tuple.getT1();
                    List<AdminPolicyDto> policyList = tuple.getT2();
                    List<AdminClaimDto> claimList = tuple.getT3();
                    List<AdminKafkaEventDto> eventList = tuple.getT4();

                    long approvedClaims = claimList.stream().filter(c -> "APPROVED".equals(c.getStatus()) || "PAID".equals(c.getStatus())).count();
                    long pendingClaims = claimList.stream().filter(c -> "PENDING".equals(c.getStatus()) || "VALIDATING".equals(c.getStatus())).count();
                    long totalPayouts = claimList.stream()
                            .filter(c -> "PAID".equals(c.getStatus()) || "APPROVED".equals(c.getStatus()))
                            .mapToLong(AdminClaimDto::getAmount)
                            .sum();

                    Map<String, Number> metrics = new LinkedHashMap<>();
                    metrics.put("activePolicies", policyList.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count());
                    metrics.put("pendingClaims", pendingClaims);
                    metrics.put("approvedClaims", approvedClaims);
                    metrics.put("totalPayouts", totalPayouts);
                    metrics.put("activeWorkers", workerList.stream().filter(w -> "ACTIVE".equals(w.getStatus())).count());
                    metrics.put("todayEvents", eventList.stream()
                            .filter(event -> event.getTimestamp().isAfter(Instant.now().minusSeconds(86400)))
                            .count());
                    metrics.put("totalWorkers", workerList.size());
                    metrics.put("totalPolicies", policyList.size());
                    metrics.put("totalClaims", claimList.size());
                    return metrics;
                });
    }

    private Mono<AdminServiceHealthDto> serviceHealth(String name, int port, String baseUrl) {
        long start = System.nanoTime();
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(Map.class)
                .map(payload -> AdminServiceHealthDto.builder()
                        .name(name)
                        .status("UP")
                        .port(port)
                        .responseTime((System.nanoTime() - start) / 1_000_000)
                        .details(Map.of("baseUrl", baseUrl, "status", String.valueOf(payload.get("status"))))
                        .build())
                .onErrorResume(e -> Mono.just(AdminServiceHealthDto.builder()
                        .name(name)
                        .status("DOWN")
                        .port(port)
                        .responseTime((System.nanoTime() - start) / 1_000_000)
                        .details(Map.of("baseUrl", baseUrl, "error", e.getMessage()))
                        .build()));
    }

    private AdminWorkerDto toAdminWorker(Worker worker) {
        String city = worker.getLocation() != null ? worker.getLocation().getCity() : null;
        return AdminWorkerDto.builder()
                .id(worker.getId())
                .name(worker.getName())
                .email(worker.getEmail())
                .phone(worker.getPhone())
                .city(city)
                .gigType(worker.getPlatform())
                .status(worker.getStatus() != null ? worker.getStatus().name() : "UNKNOWN")
                .registeredAt(worker.getCreatedAt())
                .build();
    }

    private AdminPolicyDto toAdminPolicy(Policy policy) {
        return AdminPolicyDto.builder()
                .id(policy.getId())
                .workerId(policy.getWorkerId())
                .planType("WEEKLY")
                .city(policy.getCity())
                .status(policy.getStatus() != null ? policy.getStatus().name() : "UNKNOWN")
                .premium(policy.getPremium() != null ? policy.getPremium().intValue() : 0)
                .coverage(policy.getCoverageLimit() != null ? policy.getCoverageLimit().intValue() : 0)
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .build();
    }

    private AdminClaimDto toAdminClaim(Claim claim) {
        String city = claim.getTriggerData() != null ? claim.getTriggerData().getLocation() : null;
        return AdminClaimDto.builder()
                .id(claim.getId())
                .policyId(claim.getPolicyId())
                .workerId(claim.getWorkerId())
                .triggerType(claim.getTriggerType() != null ? claim.getTriggerType().name() : "UNKNOWN")
                .status(claim.getStatus() != null ? claim.getStatus().name() : "UNKNOWN")
                .amount(claim.getAmount() != null ? claim.getAmount().intValue() : 0)
                .city(city)
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private AdminKafkaEventDto toEvent(Simulation simulation) {
        Instant timestamp = defaultInstant(simulation.getCreatedAt());
        return AdminKafkaEventDto.builder()
                .id("evt-" + simulation.getId())
                .topic("environment-disruptions")
                .type("environment." + simulation.getEventType().toLowerCase())
                .timestamp(timestamp)
                .payload(Map.of(
                        "simulationId", simulation.getId(),
                        "city", simulation.getCity(),
                        "eventType", simulation.getEventType(),
                        "severity", simulation.getSeverity(),
                        "simulatedValue", simulation.getSimulatedValue(),
                        "status", simulation.getStatus(),
                        "claimsTriggered", simulation.getClaimsTriggered()
                ))
                .build();
    }

    private Instant defaultInstant(Instant value) {
        return value != null ? value : Instant.now();
    }
}
