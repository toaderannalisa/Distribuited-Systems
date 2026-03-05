package com.example.chatbot.controllers;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatbot.services.AiChatbotService;
import com.example.chatbot.services.RuleBasedChatbotService;

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
        String ruleAnswer = chatbotService.getAnswer(question);
        // Dacă răspunsul este "Sorry, I don't understand...", folosește AI-ul
        if (ruleAnswer.equals("Sorry, I don't understand. Can you rephrase?")) {
            if (geminiApiKey != null && !geminiApiKey.isBlank()) {
                if (aiService == null) {
                    aiService = new AiChatbotService(geminiApiKey);
                }
                String aiAnswer = Optional.ofNullable(aiService.getAiAnswer(question)).orElse("");
                if (!aiAnswer.isBlank()) {
                    return Map.of("answer", aiAnswer);
                }
            }
        }
        return Map.of("answer", ruleAnswer);
    }
}
