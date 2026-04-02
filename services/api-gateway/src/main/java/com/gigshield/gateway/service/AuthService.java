package com.gigshield.gateway.service;

import com.gigshield.common.dto.AuthRequest;
import com.gigshield.common.dto.AuthResponse;
import com.gigshield.common.dto.WorkerResponse;
import com.gigshield.common.model.Worker;
import com.gigshield.gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final WorkerService workerService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating worker with identifier: {}", request.getIdentifier());
        
        Worker worker = workerService.findByIdentifier(request.getIdentifier());
        
        if (!passwordEncoder.matches(request.getPassword(), worker.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        
        if (worker.getStatus() != Worker.WorkerStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active");
        }
        
        String accessToken = jwtTokenProvider.generateAccessToken(worker);
        String refreshToken = jwtTokenProvider.generateRefreshToken(worker);
        
        log.info("Authentication successful for worker: {}", worker.getId());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .worker(WorkerResponse.fromWorker(worker))
                .build();
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        String workerId = jwtTokenProvider.getWorkerIdFromToken(refreshToken);
        WorkerResponse workerResponse = workerService.getWorkerById(workerId);
        Worker worker = workerService.findByIdentifier(workerResponse.getEmail());
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(worker);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .worker(workerResponse)
                .build();
    }
}
