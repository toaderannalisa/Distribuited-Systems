package com.example.demo.controllers;

import com.example.demo.entities.EnergyMeasurement;
import com.example.demo.dtos.HourlyEnergyConsumptionDTO;
import com.example.demo.services.EnergyConsumptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/energy")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EnergyController {

    @Autowired
    private EnergyConsumptionService energyConsumptionService;


    @GetMapping("/measurements/{deviceId}")
    public ResponseEntity<List<EnergyMeasurement>> getAllMeasurements(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching ALL measurements for device={} from {} to {}", deviceId, startDate, endDate);
        List<EnergyMeasurement> data = energyConsumptionService.getAllMeasurementsByDateRange(deviceId, startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/measurements/{deviceId}/latest")
    public ResponseEntity<List<EnergyMeasurement>> getLatestMeasurements(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("Fetching latest {} hours of measurements for device={}", hours, deviceId);
        List<EnergyMeasurement> data = energyConsumptionService.getLatestMeasurements(deviceId, hours);
        return ResponseEntity.ok(data);
    }

    // New endpoint: Get hourly averages (aggregated data)
    @GetMapping("/measurements/{deviceId}/hourly")
    public ResponseEntity<List<Map<String, Object>>> getHourlyAverages(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching hourly averages for device={} from {} to {}", deviceId, startDate, endDate);
        List<Map<String, Object>> data = energyConsumptionService.getHourlyAverages(deviceId, startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Monitoring Microservice");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
