package com.gigshield.simulator.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminServiceHealthDto {
    private String name;
    private String status;
    private int port;
    private Long responseTime;
    private Map<String, String> details;
}
