package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Data Collection Queue
    public static final String ENERGY_DATA_QUEUE = "energy.data.queue";
    public static final String ENERGY_EXCHANGE = "energy.exchange";
    public static final String ENERGY_ROUTING_KEY = "energy.data.*";

    // Device Sync Queue 
    public static final String DEVICE_SYNC_QUEUE = "device.sync.queue";

    // Energy Data Queue
    @Bean
    public Queue energyDataQueue() {
        return new Queue(ENERGY_DATA_QUEUE, true, false, false);
    }

    @Bean
    public TopicExchange energyExchange() {
        return new TopicExchange(ENERGY_EXCHANGE, true, false);
    }

    @Bean
    public Binding energyBinding(Queue energyDataQueue, TopicExchange energyExchange) {
        return BindingBuilder.bind(energyDataQueue)
                .to(energyExchange)
                .with(ENERGY_ROUTING_KEY);
    }

    // Device Sync Queue 
    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(DEVICE_SYNC_QUEUE, true, false, false);
    }
}
