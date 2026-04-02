package com.gigshield.gateway.service;

import com.gigshield.common.dto.AuthRequest;
import com.gigshield.common.dto.AuthResponse;
import com.gigshield.common.dto.WorkerResponse;
import com.gigshield.common.model.Worker;
import com.gigshield.gateway.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private WorkerService workerService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private Worker activeWorker;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        activeWorker = Worker.builder()
                .id("worker123")
                .email("test@example.com")
                .phone("+919876543210")
                .name("Test Worker")
                .passwordHash("hashedPassword123")
                .status(Worker.WorkerStatus.ACTIVE)
                .build();

        authRequest = AuthRequest.builder()
                .identifier("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void authenticate_Success() {
        when(workerService.findByIdentifier("test@example.com")).thenReturn(activeWorker);
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(Worker.class))).thenReturn("access_token_123");
        when(jwtTokenProvider.generateRefreshToken(any(Worker.class))).thenReturn("refresh_token_123");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400L);

        AuthResponse response = authService.authenticate(authRequest);

        assertThat(response.getAccessToken()).isEqualTo("access_token_123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token_123");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getWorker()).isNotNull();
        assertThat(response.getWorker().getId()).isEqualTo("worker123");
    }

    @Test
    void authenticate_InvalidPassword_ThrowsException() {
        when(workerService.findByIdentifier("test@example.com")).thenReturn(activeWorker);
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(authRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void authenticate_InactiveAccount_ThrowsException() {
        Worker inactiveWorker = Worker.builder()
                .id("worker123")
                .email("test@example.com")
                .passwordHash("hashedPassword123")
                .status(Worker.WorkerStatus.INACTIVE)
                .build();

        when(workerService.findByIdentifier("test@example.com")).thenReturn(inactiveWorker);
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(true);

        assertThatThrownBy(() -> authService.authenticate(authRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Account is not active");
    }

    @Test
    void authenticate_SuspendedAccount_ThrowsException() {
        Worker suspendedWorker = Worker.builder()
                .id("worker123")
                .email("test@example.com")
                .passwordHash("hashedPassword123")
                .status(Worker.WorkerStatus.SUSPENDED)
                .build();

        when(workerService.findByIdentifier("test@example.com")).thenReturn(suspendedWorker);
        when(passwordEncoder.matches("password123", "hashedPassword123")).thenReturn(true);

        assertThatThrownBy(() -> authService.authenticate(authRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Account is not active");
    }

    @Test
    void refreshToken_Success() {
        WorkerResponse workerResponse = WorkerResponse.fromWorker(activeWorker);
        
        when(jwtTokenProvider.validateToken("valid_refresh_token")).thenReturn(true);
        when(jwtTokenProvider.getWorkerIdFromToken("valid_refresh_token")).thenReturn("worker123");
        when(workerService.getWorkerById("worker123")).thenReturn(workerResponse);
        when(workerService.findByIdentifier(anyString())).thenReturn(activeWorker);
        when(jwtTokenProvider.generateAccessToken(any(Worker.class))).thenReturn("new_access_token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400L);

        AuthResponse response = authService.refreshToken("valid_refresh_token");

        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("valid_refresh_token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        when(jwtTokenProvider.validateToken("invalid_refresh_token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("invalid_refresh_token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid refresh token");
    }
}
