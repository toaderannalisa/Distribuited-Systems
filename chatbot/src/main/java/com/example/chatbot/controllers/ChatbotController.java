package com.example.chatbot.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Optional;
import com.example.chatbot.services.RuleBasedChatbotService;
import com.example.chatbot.services.AiChatbotService;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    private final RuleBasedChatbotService chatbotService = new RuleBasedChatbotService();

    // Inject Gemini API key from environment or properties (recommended)
    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private AiChatbotService aiService;

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        String answer;
        logger.info("[CONTROLLER DEBUG] Received question: '{}'", question);
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            logger.info("[CONTROLLER DEBUG] Using Gemini API key.");
            if (aiService == null) {
                aiService = new AiChatbotService(geminiApiKey);
            }
            String aiAnswer = Optional.ofNullable(aiService.getAiAnswer(question)).orElse("");
            logger.info("[CONTROLLER DEBUG] AI answer: '{}'", aiAnswer);
            if (!aiAnswer.isBlank()) {
                answer = aiAnswer;
                logger.info("[CONTROLLER DEBUG] Responding with AI answer.");
            } else {
                answer = chatbotService.getAnswer(question);
                logger.info("[CONTROLLER DEBUG] AI answer blank, using rule-based answer: '{}'", answer);
            }
        } else {
            answer = chatbotService.getAnswer(question);
            logger.info("[CONTROLLER DEBUG] No API key, using rule-based answer: '{}'", answer);
        }
        return Map.of("answer", answer);
    }
}
