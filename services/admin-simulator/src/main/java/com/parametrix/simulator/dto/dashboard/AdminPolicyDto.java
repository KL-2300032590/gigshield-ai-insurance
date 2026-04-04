package com.parametrix.simulator.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPolicyDto {
    private String id;
    private String workerId;
    private String planType;
    private String city;
    private String status;
    private Integer premium;
    private Integer coverage;
    private LocalDate startDate;
    private LocalDate endDate;
}
