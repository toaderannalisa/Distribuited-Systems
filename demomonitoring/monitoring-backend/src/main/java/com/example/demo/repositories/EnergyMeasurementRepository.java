package com.example.demo.repositories;

import com.example.demo.entities.EnergyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnergyMeasurementRepository extends JpaRepository<EnergyMeasurement, Long> {

    List<EnergyMeasurement> findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(
            String deviceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<EnergyMeasurement> findByDeviceIdOrderByTimestampDesc(String deviceId);
    
    List<EnergyMeasurement> findTop10ByDeviceIdOrderByTimestampDesc(String deviceId);
    
    long countByDeviceId(String deviceId);
}
