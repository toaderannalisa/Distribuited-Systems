package com.example.demo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // User Sync Queue (published to Device Backend)
    public static final String USER_SYNC_QUEUE = "user.sync.queue";

    @Bean
    public Queue userSyncQueue() {
        return new Queue(USER_SYNC_QUEUE, true, false, false);
    }
}
