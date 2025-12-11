package com.example.demo.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users_sync")
public class UserSync {
    
    @Id
    @Column(name = "user_id", length = 64)
    private String userId;
    
    @Column(nullable = false)
    private String username;

    public UserSync() {
    }

    public UserSync(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
