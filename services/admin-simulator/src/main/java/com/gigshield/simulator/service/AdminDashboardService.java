package com.gigshield.simulator.service;

import com.gigshield.simulator.dto.dashboard.AdminClaimDto;
import com.gigshield.simulator.dto.dashboard.AdminKafkaEventDto;
import com.gigshield.simulator.dto.dashboard.AdminLogDto;
import com.gigshield.simulator.dto.dashboard.AdminPolicyDto;
import com.gigshield.simulator.dto.dashboard.AdminServiceHealthDto;
import com.gigshield.simulator.dto.dashboard.AdminWorkerDto;
import com.gigshield.simulator.model.Simulation;
import com.gigshield.simulator.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final List<String> CITIES = List.of("Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai", "Kolkata", "Pune");
    private static final List<String> GIG_TYPES = List.of("DELIVERY", "RIDE_SHARE", "FOOD_DELIVERY");
    private static final List<String> PLAN_TYPES = List.of("BRONZE", "SILVER", "GOLD", "PLATINUM");

    private final SimulationRepository simulationRepository;

    public Mono<List<AdminServiceHealthDto>> getServicesHealth() {
        return Mono.just(List.of(
                serviceHealth("API Gateway", 8080, "UP"),
                serviceHealth("Risk Engine", 8081, "UP"),
                serviceHealth("Trigger Engine", 8082, "UP"),
                serviceHealth("Fraud Detection", 8083, "UP"),
                serviceHealth("Claim Service", 8084, "UP"),
                serviceHealth("Payout Service", 8085, "UP"),
                serviceHealth("Admin Simulator", 8091, "UP")
        ));
    }

    public Mono<AdminServiceHealthDto> getServiceHealth(String serviceName) {
        return getServicesHealth()
                .map(services -> services.stream()
                        .filter(service -> service.getName().equalsIgnoreCase(serviceName.replace('-', ' ')))
                        .findFirst()
                        .orElse(serviceHealth(serviceName, 0, "UNKNOWN")));
    }

    public Mono<List<AdminWorkerDto>> getWorkers() {
        return simulationRepository.findAll().collectList().map(this::buildWorkers);
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
        return simulationRepository.findAll().collectList().map(this::buildPolicies);
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
        return simulationRepository.findAll().collectList().map(simulations -> {
            List<AdminClaimDto> claims = buildClaims(simulations);
            return claims.stream()
                    .filter(claim -> status.map(s -> claim.getStatus().equalsIgnoreCase(s)).orElse(true))
                    .filter(claim -> city.map(c -> claim.getCity().equalsIgnoreCase(c)).orElse(true))
                    .toList();
        });
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

    private AdminServiceHealthDto serviceHealth(String name, int port, String status) {
        return AdminServiceHealthDto.builder()
                .name(name)
                .status(status)
                .port(port)
                .responseTime(ThreadLocalRandom.current().nextLong(12, 85))
                .details(Map.of("version", "1.0.0", "environment", "local"))
                .build();
    }

    private List<AdminWorkerDto> buildWorkers(List<Simulation> simulations) {
        Instant now = Instant.now();
        int baseCount = Math.max(24, simulations.size() * 3);

        return IntStream.range(0, baseCount)
                .mapToObj(index -> {
                    String city = CITIES.get(index % CITIES.size());
                    String gigType = GIG_TYPES.get(index % GIG_TYPES.size());
                    return AdminWorkerDto.builder()
                            .id(String.format("W-%04d", index + 1))
                            .name(String.format("Worker %02d", index + 1))
                            .email(String.format("worker%02d@gigshield.ai", index + 1))
                            .phone(String.format("900000%04d", index))
                            .city(city)
                            .gigType(gigType)
                            .status(index % 7 == 0 ? "INACTIVE" : "ACTIVE")
                            .registeredAt(now.minusSeconds((long) index * 8640))
                            .build();
                })
                .toList();
    }

    private List<AdminPolicyDto> buildPolicies(List<Simulation> simulations) {
        List<AdminWorkerDto> workers = buildWorkers(simulations);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        return workers.stream().limit(Math.max(18, workers.size() - 4))
                .map(worker -> {
                    int index = Integer.parseInt(worker.getId().substring(2));
                    String planType = PLAN_TYPES.get(index % PLAN_TYPES.size());
                    int premium = switch (planType) {
                        case "BRONZE" -> 299;
                        case "SILVER" -> 599;
                        case "GOLD" -> 999;
                        default -> 1499;
                    };
                    int coverage = switch (planType) {
                        case "BRONZE" -> 15000;
                        case "SILVER" -> 30000;
                        case "GOLD" -> 50000;
                        default -> 100000;
                    };
                    boolean expired = index % 11 == 0;
                    return AdminPolicyDto.builder()
                            .id(String.format("POL-%04d", index))
                            .workerId(worker.getId())
                            .planType(planType)
                            .city(worker.getCity())
                            .status(expired ? "EXPIRED" : "ACTIVE")
                            .premium(premium)
                            .coverage(coverage)
                            .startDate(today.minusWeeks(index % 12 + 1L))
                            .endDate(today.plusWeeks(expired ? -1 : 1))
                            .build();
                })
                .toList();
    }

    private List<AdminClaimDto> buildClaims(List<Simulation> simulations) {
        List<AdminPolicyDto> policies = buildPolicies(simulations);
        List<AdminClaimDto> claims = new ArrayList<>();

        int sequence = 1;
        for (Simulation simulation : simulations.stream().sorted(Comparator.comparing(Simulation::getCreatedAt).reversed()).toList()) {
            AdminPolicyDto policy = policies.get(sequence % policies.size());
            String status = claimStatusFromSimulation(simulation.getStatus(), sequence);
            int amount = (int) Math.max(1500, Math.min(12000, Math.round(simulation.getSimulatedValue() * 80)));
            Instant createdAt = defaultInstant(simulation.getCreatedAt());
            claims.add(AdminClaimDto.builder()
                    .id(String.format("CLM-%04d", sequence))
                    .policyId(policy.getId())
                    .workerId(policy.getWorkerId())
                    .triggerType(simulation.getEventType())
                    .status(status)
                    .amount(amount)
                    .city(simulation.getCity())
                    .createdAt(createdAt)
                    .updatedAt(defaultInstant(simulation.getCompletedAt()))
                    .build());
            sequence++;
        }

        while (claims.size() < 20) {
            AdminPolicyDto policy = policies.get(sequence % policies.size());
            Instant createdAt = Instant.now().minusSeconds((long) sequence * 3600);
            claims.add(AdminClaimDto.builder()
                    .id(String.format("CLM-%04d", sequence))
                    .policyId(policy.getId())
                    .workerId(policy.getWorkerId())
                    .triggerType(sequence % 2 == 0 ? "HEAVY_RAIN" : "HIGH_AQI")
                    .status(sequence % 5 == 0 ? "PENDING" : sequence % 3 == 0 ? "PAID" : "APPROVED")
                    .amount(2500 + (sequence % 8) * 700)
                    .city(policy.getCity())
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plusSeconds(1800))
                    .build());
            sequence++;
        }

        return claims.stream()
                .sorted(Comparator.comparing(AdminClaimDto::getCreatedAt).reversed())
                .toList();
    }

    private String claimStatusFromSimulation(String simulationStatus, int index) {
        if (Objects.equals(simulationStatus, "FAILED")) {
            return "REJECTED";
        }
        if (Objects.equals(simulationStatus, "IN_PROGRESS")) {
            return "VALIDATING";
        }
        if (index % 4 == 0) {
            return "PAID";
        }
        if (index % 5 == 0) {
            return "PENDING";
        }
        return "APPROVED";
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
