package com.example.demo.services;

import com.example.demo.dtos.EnergyMeasurementDTO;
import com.example.demo.entities.EnergyMeasurement;
import com.example.demo.repositories.EnergyMeasurementRepository;
import com.example.demo.repositories.DeviceSyncRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.web.client.RestTemplate;
import com.example.demo.entities.DeviceSync;

@Service
@Slf4j
public class EnergyConsumptionService {

    @Autowired
    private EnergyMeasurementRepository energyMeasurementRepository;

    @Autowired
    private DeviceSyncRepository deviceSyncRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "energy.data.queue")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void processEnergyData(EnergyMeasurementDTO measurement) {
        log.info("Processing energy measurement: Device={}, Value={} kWh, Timestamp={}",
                measurement.getDeviceId(), measurement.getMeasurementValue(), measurement.getTimestamp());

        // 1. Validate device exists
        boolean deviceExists = deviceSyncRepository.existsById(measurement.getDeviceId());

        if (!deviceExists) {
            log.warn("Device ID {} not found in devices_sync table. Skipping measurement.", measurement.getDeviceId());
            return;
        }

        // 2. Save raw measurement to database
        EnergyMeasurement entity = EnergyMeasurement.builder()
            .deviceId(measurement.getDeviceId())
                .measurementValue(measurement.getMeasurementValue())
                .timestamp(measurement.getTimestamp())
                .build();
        
        energyMeasurementRepository.save(entity);
        log.info("Saved measurement: Device={}, Value={} kWh, Timestamp={}",
            measurement.getDeviceId(), measurement.getMeasurementValue(), measurement.getTimestamp());

        // 3. Send real-time update via WebSocket
        sendRealtimeUpdate(measurement.getDeviceId(), measurement.getMeasurementValue(), measurement.getTimestamp());

        // 4. Detect overconsumption and send notification if needed
        checkAndNotifyOverconsumption(measurement.getDeviceId(), measurement.getMeasurementValue());
    }

    private void sendRealtimeUpdate(String deviceId, Double currentValue, LocalDateTime timestamp) {
        try {
            // Get last 10 measurements to calculate average (10 measurements = 1 "hour" on chart)
            List<EnergyMeasurement> recentMeasurements = energyMeasurementRepository
                .findTop10ByDeviceIdOrderByTimestampDesc(deviceId);
            
            Double hourlyAverage = null;
            int hourIndex = 0;
            
            if (!recentMeasurements.isEmpty()) {
                hourlyAverage = recentMeasurements.stream()
                    .mapToDouble(EnergyMeasurement::getMeasurementValue)
                    .average()
                    .orElse(0.0);
                
                // Calculate which "hour" bucket this is (every 10 measurements = 1 hour)
                long totalCount = energyMeasurementRepository.countByDeviceId(deviceId);
                hourIndex = (int) Math.ceil(totalCount / 10.0);
            }
            
            Map<String, Object> update = new HashMap<>();
            update.put("deviceId", deviceId);
            update.put("consumption", currentValue);
            update.put("timestamp", timestamp);
            update.put("hourlyAverage", hourlyAverage);
            update.put("hourlyCount", recentMeasurements.size());
            update.put("hourIndex", hourIndex);

            log.debug("Sending realtime update to /topic/energy/device/{}: value={} kWh, hourlyAvg={} kWh ({}/10 measurements, hour {})", 
                    deviceId, currentValue, hourlyAverage, recentMeasurements.size(), hourIndex);
            messagingTemplate.convertAndSend("/topic/energy/device/" + deviceId, (Object) update);
        } catch (Exception e) {
            log.warn("Failed to send WebSocket update: {}", e.getMessage());
        }
    }

    // API method to get ALL raw measurements for a device in date range
    public List<EnergyMeasurement> getAllMeasurementsByDateRange(
            String deviceId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        log.info("Querying all measurements: deviceId={}, start={}, end={}", 
                deviceId, startDateTime, endDateTime);

        List<EnergyMeasurement> measurements = energyMeasurementRepository
                .findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(
                        deviceId, startDateTime, endDateTime);
        
        log.info("Found {} measurements", measurements.size());

        return measurements;
    }

    public List<EnergyMeasurement> getLatestMeasurements(String deviceId, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        List<EnergyMeasurement> all = energyMeasurementRepository
                .findByDeviceIdOrderByTimestampDesc(deviceId);

        return all.stream()
                .filter(m -> m.getTimestamp().isAfter(startTime))
                .toList();
    }

    // New method: Get hourly averages for a device in date range
    public List<Map<String, Object>> getHourlyAverages(
            String deviceId, LocalDate startDate, LocalDate endDate) {
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        log.info("Calculating hourly averages: deviceId={}, start={}, end={}", 
                deviceId, startDateTime, endDateTime);

        List<EnergyMeasurement> measurements = energyMeasurementRepository
                .findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(
                        deviceId, startDateTime, endDateTime);

        // Group by hour and calculate average
        Map<String, List<Double>> hourlyData = new LinkedHashMap<>();
        
        for (EnergyMeasurement m : measurements) {
            String hourKey = String.format("%02d:00", m.getTimestamp().getHour());
            hourlyData.computeIfAbsent(hourKey, k -> new ArrayList<>())
                      .add(m.getMeasurementValue());
        }

        // Calculate averages
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : hourlyData.entrySet()) {
            double avg = entry.getValue().stream()
                              .mapToDouble(Double::doubleValue)
                              .average()
                              .orElse(0.0);
            
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", entry.getKey());
            hourData.put("averageValue", Math.round(avg * 100.0) / 100.0);
            hourData.put("count", entry.getValue().size());
            result.add(hourData);
        }

        log.info("Calculated {} hourly averages", result.size());
        return result;
    }



    private void checkAndNotifyOverconsumption(String deviceId, Double value) {
        if (value == null) {
            return;
        }
        Optional<DeviceSync> deviceOpt = deviceSyncRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            log.warn("Nu s-a găsit device {} pentru notificare overconsumption.", deviceId);
            return;
        }
        DeviceSync device = deviceOpt.get();
        Double threshold = device.getMaxConsumption();
        log.info("Device {} maxConsumption: {}", deviceId, threshold);
        if (threshold == null) {
            log.warn("Device {} nu are maxConsumption setat.", deviceId);
            return;
        }
        if (value < threshold) {
            return;
        }
        String personId = device.getUserId();
        if (personId == null || personId.isEmpty()) {
            log.warn("Device {} nu are userId asociat pentru notificare.", deviceId);
            return;
        }

        // Construiește payloadul pentru notificare
        Map<String, Object> notification = new HashMap<>();
        notification.put("deviceId", deviceId);
        notification.put("message", "Overconsumption detected: " + value + " kWh");
        notification.put("personId", personId);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String notificationUrl = "http://demonotification-backend:8090/api/notify";
            restTemplate.postForEntity(notificationUrl, notification, Void.class);
            log.info("Trimis notificare overconsumption pentru device {} către user {}.", deviceId, personId);
        } catch (Exception e) {
            log.error("Eroare la trimiterea notificării de overconsumption: {}", e.getMessage());
        }
    }
}
