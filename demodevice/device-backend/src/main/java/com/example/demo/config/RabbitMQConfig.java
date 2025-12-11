package com.example.demo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // User Sync Queue (consumed from Person Backend)
    public static final String USER_SYNC_QUEUE = "user.sync.queue";
    
    // Device Sync Queue (published to Monitoring Backend)
    public static final String DEVICE_SYNC_QUEUE = "device.sync.queue";

    @Bean
    public Queue userSyncQueue() {
        return new Queue(USER_SYNC_QUEUE, true, false, false);
    }

    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(DEVICE_SYNC_QUEUE, true, false, false);
    }
}
