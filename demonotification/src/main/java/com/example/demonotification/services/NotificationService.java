package com.example.demonotification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate template) {
        this.messagingTemplate = template;
    }

    public void sendNotification(NotificationMessage message) {
        messagingTemplate.convertAndSend("/topic/overconsumption", message);
    }
}
