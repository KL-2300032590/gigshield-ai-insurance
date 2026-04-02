package com.gigshield.gateway.service;

import com.gigshield.common.dto.ClaimResponse;
import com.gigshield.common.model.Claim;
import com.gigshield.gateway.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {
    
    private final ClaimRepository claimRepository;
    
    public List<ClaimResponse> getWorkerClaims(String workerId) {
        List<Claim> claims = claimRepository.findByWorkerId(workerId);
        return claims.stream()
                .map(ClaimResponse::fromClaim)
                .collect(Collectors.toList());
    }
    
    public ClaimResponse getClaimById(String id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + id));
        return ClaimResponse.fromClaim(claim);
    }
    
    public List<ClaimResponse> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(ClaimResponse::fromClaim)
                .collect(Collectors.toList());
    }
}
