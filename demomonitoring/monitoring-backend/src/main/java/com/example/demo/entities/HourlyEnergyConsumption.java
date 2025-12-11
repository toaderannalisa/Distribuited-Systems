package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_energy_consumption")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyEnergyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long deviceId;

    @Column(nullable = false)
    private Double totalEnergy;

    @Column(nullable = false)
    private Double maxConsumption;

    @Column(nullable = false)
    private LocalDateTime hourTimestamp;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
