package com.gigshield.claim.controller;

import com.gigshield.common.dto.ClaimResponse;
import com.gigshield.claim.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {
    
    private final ClaimService claimService;
    
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<ClaimResponse>> getWorkerClaims(@PathVariable String workerId) {
        List<ClaimResponse> claims = claimService.getWorkerClaims(workerId);
        return ResponseEntity.ok(claims);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable String id) {
        ClaimResponse claim = claimService.getClaimById(id);
        return ResponseEntity.ok(claim);
    }
}
