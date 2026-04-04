package com.parametrix.common.events;

import com.parametrix.common.model.Claim.TriggerType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event published when an environmental disruption is detected.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnvironmentDisruptionEvent extends BaseEvent {
    
    public static final String TYPE = "environment.disruption";
    
    private TriggerType triggerType;
    private String city;
    private double latitude;
    private double longitude;
    private double measuredValue;
    private double threshold;
    private String source;
    
    @Builder.Default
    private Instant measuredAt = Instant.now();
    
    private Severity severity;
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
