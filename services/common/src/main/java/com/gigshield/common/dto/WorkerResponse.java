package com.gigshield.common.dto;

import com.gigshield.common.model.Worker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for worker response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerResponse {
    
    private String id;
    private String name;
    private String email;
    private String phone;
    private LocationDto location;
    private String platform;
    private String platformId;
    private Worker.WorkerStatus status;
    private Instant createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private double latitude;
        private double longitude;
        private String city;
        private String state;
        private String pincode;
    }
    
    public static WorkerResponse fromWorker(Worker worker) {
        return WorkerResponse.builder()
                .id(worker.getId())
                .name(worker.getName())
                .email(worker.getEmail())
                .phone(worker.getPhone())
                .location(worker.getLocation() != null ? LocationDto.builder()
                        .latitude(worker.getLocation().getLatitude())
                        .longitude(worker.getLocation().getLongitude())
                        .city(worker.getLocation().getCity())
                        .state(worker.getLocation().getState())
                        .pincode(worker.getLocation().getPincode())
                        .build() : null)
                .platform(worker.getPlatform())
                .platformId(worker.getPlatformId())
                .status(worker.getStatus())
                .createdAt(worker.getCreatedAt())
                .build();
    }
}
