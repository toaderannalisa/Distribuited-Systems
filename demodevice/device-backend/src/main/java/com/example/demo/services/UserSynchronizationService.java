package com.example.demo.services;

import com.example.demo.entities.UserSync;
import com.example.demo.repositories.UserSyncRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserSynchronizationService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSynchronizationService.class);
    private final UserSyncRepository userSyncRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UserSynchronizationService(UserSyncRepository userSyncRepository) {
        this.userSyncRepository = userSyncRepository;
    }

    @RabbitListener(queues = "user.sync.queue")
    public void processUserSyncMessage(String message) {
        try {
            LOGGER.info("Received USER_CREATED message: {}", message);
            
            Map<String, Object> messageMap = objectMapper.readValue(message, Map.class);
            String eventType = (String) messageMap.get("eventType");
            
            if ("USER_CREATED".equals(eventType)) {
                String userId = (String) messageMap.get("userId");
                String username = (String) messageMap.get("username");
                
                UserSync userSync = new UserSync(userId, username);
                userSyncRepository.save(userSync);
                
                LOGGER.info("User synchronized to device_db: ID={}, Username={}", userId, username);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to process user sync message: {}", e.getMessage(), e);
        }
    }
}
