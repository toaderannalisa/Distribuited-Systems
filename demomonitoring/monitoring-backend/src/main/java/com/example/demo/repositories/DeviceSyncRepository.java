package com.example.demo.repositories;

import com.example.demo.entities.DeviceSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceSyncRepository extends JpaRepository<DeviceSync, String> {
    List<DeviceSync> findByUserId(String userId);
}
