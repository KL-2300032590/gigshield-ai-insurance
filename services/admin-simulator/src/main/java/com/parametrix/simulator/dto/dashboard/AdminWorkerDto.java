package com.parametrix.simulator.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWorkerDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String gigType;
    private String status;
    private Instant registeredAt;
}
