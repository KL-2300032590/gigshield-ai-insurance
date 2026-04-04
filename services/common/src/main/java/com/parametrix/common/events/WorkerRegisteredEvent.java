package com.parametrix.common.events;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a new worker registers on the platform.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WorkerRegisteredEvent extends BaseEvent {
    
    public static final String TYPE = "worker.registered";
    
    private String workerId;
    private String name;
    private String email;
    private String phone;
    private double latitude;
    private double longitude;
    private String city;
    private String platform;
}
