package com.parametrix.simulator.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminKafkaEventDto {
    private String id;
    private String topic;
    private String type;
    private Instant timestamp;
    private Map<String, Object> payload;
}
