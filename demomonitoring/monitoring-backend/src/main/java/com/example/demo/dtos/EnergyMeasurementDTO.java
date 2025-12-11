package com.example.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyMeasurementDTO {
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("measurement_value")
    private Double measurementValue;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
