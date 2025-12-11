package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "maxConsumption is required")
    @Positive(message = "maxConsumption must be positive")
    private Double maxConsumption;

    private UUID personId;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(String name, Double maxConsumption) {
        this.name = name;
        this.maxConsumption = maxConsumption;
    }

    public DeviceDetailsDTO(UUID id, String name, Double maxConsumption, UUID personId) {
        this.id = id;
        this.name = name;
        this.maxConsumption = maxConsumption;
        this.personId = personId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(Double maxConsumption) {
        this.maxConsumption = maxConsumption;
    }

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, maxConsumption);
    }
}