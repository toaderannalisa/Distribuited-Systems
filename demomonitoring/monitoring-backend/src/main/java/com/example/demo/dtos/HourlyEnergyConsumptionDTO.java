package com.example.demo.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyEnergyConsumptionDTO {
    private Long id;
    private Long deviceId;
    private Double totalEnergy;
    private Double maxConsumption;
    private LocalDateTime hourTimestamp;
    private LocalDateTime createdAt;
}
