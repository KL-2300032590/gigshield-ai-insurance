package com.parametrix.gateway.service;

import com.parametrix.common.dto.WorkerRegistrationRequest;
import com.parametrix.common.dto.WorkerResponse;
import com.parametrix.common.model.Worker;
import com.parametrix.gateway.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private WorkerService workerService;

    private WorkerRegistrationRequest registrationRequest;
    private Worker worker;

    @BeforeEach
    void setUp() {
        registrationRequest = WorkerRegistrationRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("9876543210")
                .password("password123")
                .latitude(19.076)
                .longitude(72.877)
                .city("Mumbai")
                .platform("SWIGGY")
                .build();

        worker = Worker.builder()
                .id("worker123")
                .name("John Doe")
                .email("john@example.com")
                .phone("9876543210")
                .passwordHash("hashedPassword")
                .location(Worker.Location.builder()
                        .latitude(19.076)
                        .longitude(72.877)
                        .city("Mumbai")
                        .build())
                .platform("SWIGGY")
                .status(Worker.WorkerStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void registerWorker_Success() {
        when(workerRepository.existsByEmail(any())).thenReturn(false);
        when(workerRepository.existsByPhone(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(workerRepository.save(any())).thenReturn(worker);

        WorkerResponse response = workerService.registerWorker(registrationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getStatus()).isEqualTo(Worker.WorkerStatus.ACTIVE);

        verify(workerRepository).save(any(Worker.class));
        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    void registerWorker_EmailAlreadyExists_ThrowsException() {
        when(workerRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> workerService.registerWorker(registrationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void registerWorker_PhoneAlreadyExists_ThrowsException() {
        when(workerRepository.existsByEmail(any())).thenReturn(false);
        when(workerRepository.existsByPhone(any())).thenReturn(true);

        assertThatThrownBy(() -> workerService.registerWorker(registrationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone number already registered");
    }

    @Test
    void getWorkerById_Success() {
        when(workerRepository.findById("worker123")).thenReturn(Optional.of(worker));

        WorkerResponse response = workerService.getWorkerById("worker123");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("worker123");
    }

    @Test
    void getWorkerById_NotFound_ThrowsException() {
        when(workerRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workerService.getWorkerById("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Worker not found");
    }
}
