package com.gigshield.simulator.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogDto {
    private String id;
    private String level;
    private String service;
    private String message;
    private Instant timestamp;
}
