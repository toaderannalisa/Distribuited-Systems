package com.example.demo.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSyncDTO {
    private String eventType;
    private String deviceId;
    private String userId;
    private String description;
    private Double maxConsumption;
}
