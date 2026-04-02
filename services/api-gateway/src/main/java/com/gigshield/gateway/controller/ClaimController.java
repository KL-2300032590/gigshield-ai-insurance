package com.gigshield.gateway.controller;

import com.gigshield.common.dto.ClaimResponse;
import com.gigshield.gateway.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Claim management endpoints")
public class ClaimController {
    
    private final ClaimService claimService;
    
    @GetMapping("/worker/{workerId}")
    @Operation(summary = "Get all claims for a worker")
    public ResponseEntity<List<ClaimResponse>> getWorkerClaims(@PathVariable String workerId) {
        List<ClaimResponse> claims = claimService.getWorkerClaims(workerId);
        return ResponseEntity.ok(claims);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable String id) {
        ClaimResponse claim = claimService.getClaimById(id);
        return ResponseEntity.ok(claim);
    }
    
    @GetMapping
    @Operation(summary = "Get all claims")
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        List<ClaimResponse> claims = claimService.getAllClaims();
        return ResponseEntity.ok(claims);
    }
}
