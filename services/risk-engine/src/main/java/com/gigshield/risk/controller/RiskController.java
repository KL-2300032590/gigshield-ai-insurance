package com.gigshield.risk.controller;

import com.gigshield.common.dto.RiskPremiumResponse;
import com.gigshield.risk.service.RiskCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {
    
    private final RiskCalculationService riskCalculationService;
    
    @GetMapping("/premium/{workerId}")
    public ResponseEntity<RiskPremiumResponse> getRiskPremium(
            @PathVariable String workerId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "0") double latitude,
            @RequestParam(required = false, defaultValue = "0") double longitude) {
        
        RiskPremiumResponse response = riskCalculationService.calculateRiskPremium(
                workerId, city, latitude, longitude);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<RiskPremiumResponse> calculateRisk(
            @RequestBody RiskCalculationRequest request) {
        
        RiskPremiumResponse response = riskCalculationService.calculateRiskPremium(
                request.getWorkerId(),
                request.getCity(),
                request.getLatitude(),
                request.getLongitude());
        
        return ResponseEntity.ok(response);
    }
    
    @lombok.Data
    public static class RiskCalculationRequest {
        private String workerId;
        private String city;
        private double latitude;
        private double longitude;
    }
}
