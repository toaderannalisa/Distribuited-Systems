package com.example.demo.dtos;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {

    private UUID id;
    private String name;
    private Double maxConsumption;
    private UUID personId;

    public DeviceDTO() {
    }

    public DeviceDTO(UUID id, String name, Double maxConsumption, UUID personId) {
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