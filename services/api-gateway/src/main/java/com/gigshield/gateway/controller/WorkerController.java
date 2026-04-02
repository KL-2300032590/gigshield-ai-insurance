package com.gigshield.gateway.controller;

import com.gigshield.common.dto.WorkerRegistrationRequest;
import com.gigshield.common.dto.WorkerResponse;
import com.gigshield.gateway.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@Tag(name = "Workers", description = "Worker management endpoints")
public class WorkerController {
    
    private final WorkerService workerService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new worker")
    public ResponseEntity<WorkerResponse> registerWorker(
            @Valid @RequestBody WorkerRegistrationRequest request) {
        WorkerResponse response = workerService.registerWorker(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current worker profile")
    public ResponseEntity<WorkerResponse> getCurrentWorker(
            @AuthenticationPrincipal String workerId) {
        WorkerResponse response = workerService.getWorkerById(workerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get worker by ID")
    public ResponseEntity<WorkerResponse> getWorkerById(@PathVariable String id) {
        WorkerResponse response = workerService.getWorkerById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all workers")
    public ResponseEntity<List<WorkerResponse>> getAllWorkers() {
        List<WorkerResponse> workers = workerService.getAllWorkers();
        return ResponseEntity.ok(workers);
    }
}
