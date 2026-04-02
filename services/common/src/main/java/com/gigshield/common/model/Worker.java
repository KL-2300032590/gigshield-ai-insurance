package com.gigshield.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Worker domain model representing a gig delivery worker.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "workers")
public class Worker {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    @Indexed(unique = true)
    private String phone;
    
    private String name;
    private String passwordHash;
    private Location location;
    private String platformId;  // Swiggy/Zomato worker ID
    private String platform;    // SWIGGY, ZOMATO, etc.
    private WorkerStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private double latitude;
        private double longitude;
        private String city;
        private String state;
        private String pincode;
    }
    
    public enum WorkerStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }
}
