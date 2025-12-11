package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_measurements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String deviceId;

    @Column(nullable = false)
    private Double measurementValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
