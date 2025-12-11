package com.example.demo.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class EnergyWebSocketController {

    @MessageMapping("/subscribe/device/{deviceId}")
    public void subscribeToDevice(@DestinationVariable Long deviceId) {
        log.info("Client subscribed to device energy updates: deviceId={}", deviceId);
    }

    @SubscribeMapping("/topic/energy/device/{deviceId}")
    public Map<String, Object> subscriptionConfirmation(@DestinationVariable Long deviceId) {
        log.info("WebSocket subscription confirmed for device: {}", deviceId);
        Map<String, Object> response = new HashMap<>();
        response.put("deviceId", deviceId);
        response.put("message", "Connected to energy monitoring");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
