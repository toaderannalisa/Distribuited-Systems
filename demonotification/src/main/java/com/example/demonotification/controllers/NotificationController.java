package com.example.demonotification;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public void notifyOverconsumption(@RequestBody NotificationMessage message) {
        notificationService.sendNotification(message);
    }
}
