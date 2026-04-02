package com.gigshield.gateway.controller;

import com.gigshield.common.dto.PolicyPurchaseRequest;
import com.gigshield.common.dto.PolicyResponse;
import com.gigshield.gateway.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Policy management endpoints")
public class PolicyController {
    
    private final PolicyService policyService;
    
    @PostMapping
    @Operation(summary = "Purchase a new policy")
    public ResponseEntity<PolicyResponse> purchasePolicy(
            @RequestBody PolicyPurchaseRequest request,
            @AuthenticationPrincipal String workerId) {
        PolicyResponse response = policyService.purchasePolicy(request, workerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get current active policy")
    public ResponseEntity<PolicyResponse> getActivePolicy(
            @AuthenticationPrincipal String workerId) {
        PolicyResponse response = policyService.getActivePolicy(workerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all policies for current worker")
    public ResponseEntity<List<PolicyResponse>> getMyPolicies(
            @AuthenticationPrincipal String workerId) {
        List<PolicyResponse> policies = policyService.getWorkerPolicies(workerId);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable String id) {
        PolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(response);
    }
}
