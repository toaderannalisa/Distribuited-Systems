package com.example.demo.services;

import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, RabbitTemplate rabbitTemplate) {
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(deviceOptional.get());
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        
        // Publish synchronization event to RabbitMQ
        publishDeviceSyncEvent(device);
        
        return device.getId();
    }
    
    private void publishDeviceSyncEvent(Device device) {
        try {
            Map<String, Object> syncMessage = new HashMap<>();
            syncMessage.put("eventType", "DEVICE_CREATED");
            syncMessage.put("deviceId", device.getId().toString());
            syncMessage.put("userId", device.getPersonId() != null ? device.getPersonId().toString() : null);
            syncMessage.put("description", device.getName());
            
            String message = objectMapper.writeValueAsString(syncMessage);
            rabbitTemplate.convertAndSend("device.sync.queue", message);
            
            LOGGER.info("Published DEVICE_CREATED event for device ID: {}", device.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to publish device sync event: {}", e.getMessage(), e);
        }
    }

    public UUID updateById(UUID id, DeviceDetailsDTO deviceDTO) {
        Optional<Device> existingDeviceOpt = deviceRepository.findById(id);
        if (!existingDeviceOpt.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }

        Device device = existingDeviceOpt.get();
        device.setName(deviceDTO.getName());
        device.setMaxConsumption(deviceDTO.getMaxConsumption());
        device.setPersonId(deviceDTO.getPersonId());
        device = deviceRepository.save(device);

        LOGGER.debug("Device with id {} was updated in db", device.getId());
        return device.getId();
    }

    public void deleteById(UUID id) {
        if (!deviceRepository.existsById(id)) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        deviceRepository.deleteById(id);
        LOGGER.debug("Device with id {} was deleted from db", id);
    }

    public List<DeviceDTO> findDevicesByPersonId(UUID personId) {
        List<Device> devices = deviceRepository.findByPersonId(personId);
        return devices.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }
}