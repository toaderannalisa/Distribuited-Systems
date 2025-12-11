package com.example.demo.repositories;

import com.example.demo.entities.HourlyEnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HourlyEnergyConsumptionRepository extends JpaRepository<HourlyEnergyConsumption, Long> {
    List<HourlyEnergyConsumption> findByDeviceIdAndHourTimestampBetweenOrderByHourTimestampAsc(
            Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<HourlyEnergyConsumption> findByDeviceIdOrderByHourTimestampDesc(Long deviceId);
    
    HourlyEnergyConsumption findByDeviceIdAndHourTimestamp(Long deviceId, LocalDateTime hourTimestamp);
}
