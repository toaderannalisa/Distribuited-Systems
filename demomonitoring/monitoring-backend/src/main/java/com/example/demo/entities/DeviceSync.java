package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "devices_sync")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSync {

    @Id
    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(nullable = false)
    private String description;

    @Column(name = "user_id", length = 64)
    private String userId;
}
