package com.example.demo.repositories;

import com.example.demo.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

        /**
         * Example: JPA generate query by existing field
         */
        List<Device> findByName(String name);

        /**
         * Find all devices assigned to a specific person
         */
        List<Device> findByPersonId(UUID personId);

        /**
         * Find all devices that are NOT assigned to anyone
         */
        List<Device> findByPersonIdIsNull();

        /**
         * Example: Custom query
         */
        @Query(value = "SELECT d " +
                "FROM Device d " +
                "WHERE d.name = :name")
        Optional<Device> findSeniorsByName(@Param("name") String name);
}