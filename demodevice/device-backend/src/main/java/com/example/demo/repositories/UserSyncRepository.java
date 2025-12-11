package com.example.demo.repositories;

import com.example.demo.entities.UserSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSyncRepository extends JpaRepository<UserSync, String> {
}
