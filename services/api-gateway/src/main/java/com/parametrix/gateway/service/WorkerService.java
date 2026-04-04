package com.parametrix.gateway.service;

import com.parametrix.common.dto.WorkerRegistrationRequest;
import com.parametrix.common.dto.WorkerResponse;
import com.parametrix.common.events.KafkaTopics;
import com.parametrix.common.events.WorkerRegisteredEvent;
import com.parametrix.common.model.Worker;
import com.parametrix.gateway.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService {
    
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    public WorkerResponse registerWorker(WorkerRegistrationRequest request) {
        log.info("Registering new worker with email: {}", request.getEmail());
        
        // Check for existing worker
        if (workerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (workerRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }
        
        // Create worker entity
        Worker worker = Worker.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .location(Worker.Location.builder()
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .city(request.getCity())
                        .state(request.getState())
                        .pincode(request.getPincode())
                        .build())
                .platform(request.getPlatform())
                .platformId(request.getPlatformId())
                .status(Worker.WorkerStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        worker = workerRepository.save(worker);
        log.info("Worker registered successfully with ID: {}", worker.getId());
        
        // Publish event to Kafka
        publishWorkerRegisteredEvent(worker);
        
        return WorkerResponse.fromWorker(worker);
    }
    
    public WorkerResponse getWorkerById(String id) {
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));
        return WorkerResponse.fromWorker(worker);
    }
    
    public WorkerResponse getWorkerByEmail(String email) {
        Worker worker = workerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));
        return WorkerResponse.fromWorker(worker);
    }
    
    public List<WorkerResponse> getAllWorkers() {
        return workerRepository.findAll().stream()
                .map(WorkerResponse::fromWorker)
                .collect(Collectors.toList());
    }
    
    public Worker findByIdentifier(String identifier) {
        return workerRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));
    }
    
    private void publishWorkerRegisteredEvent(Worker worker) {
        WorkerRegisteredEvent event = WorkerRegisteredEvent.builder()
                .eventType(WorkerRegisteredEvent.TYPE)
                .workerId(worker.getId())
                .name(worker.getName())
                .email(worker.getEmail())
                .phone(worker.getPhone())
                .latitude(worker.getLocation().getLatitude())
                .longitude(worker.getLocation().getLongitude())
                .city(worker.getLocation().getCity())
                .platform(worker.getPlatform())
                .build();
        
        kafkaTemplate.send(KafkaTopics.WORKER_REGISTERED, worker.getId(), event);
        log.info("Published WorkerRegisteredEvent for worker: {}", worker.getId());
    }
}
