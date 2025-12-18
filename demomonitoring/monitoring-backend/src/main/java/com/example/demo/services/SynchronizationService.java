package com.example.demo.services;

import com.example.demo.dtos.DeviceSyncDTO;
import com.example.demo.entities.DeviceSync;
import com.example.demo.repositories.DeviceSyncRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SynchronizationService {

    @Autowired
    private DeviceSyncRepository deviceSyncRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "device.sync.queue")
    public void processDeviceSynchronizationMessage(String message) {
        log.info("Processing DEVICE_CREATED message: {}", message);
        try {
            DeviceSyncDTO deviceSync = objectMapper.readValue(message, DeviceSyncDTO.class);
            syncDevice(deviceSync);
        } catch (Exception e) {
            log.error("Error processing device synchronization message: {}", e.getMessage(), e);
        }
    }

    private void syncDevice(DeviceSyncDTO deviceSyncDTO) {
        try {
            DeviceSync deviceSync = new DeviceSync();
            deviceSync.setDeviceId(deviceSyncDTO.getDeviceId());
            deviceSync.setDescription(deviceSyncDTO.getDescription());
            deviceSync.setUserId(deviceSyncDTO.getUserId());
            deviceSync.setMaxConsumption(deviceSyncDTO.getMaxConsumption());

            deviceSyncRepository.save(deviceSync);
            log.info("Device synchronized: ID={}, Description={}, UserID={}, MaxConsumption={}", 
                    deviceSyncDTO.getDeviceId(), deviceSyncDTO.getDescription(), deviceSyncDTO.getUserId(), deviceSyncDTO.getMaxConsumption());
        } catch (Exception e) {
            log.error("Error syncing device: {}", e.getMessage(), e);
        }
    }

    public DeviceSync getDeviceSync(String deviceId) {
        return deviceSyncRepository.findById(deviceId).orElse(null);
    }
}
