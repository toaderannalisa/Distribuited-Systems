package com.example.chatbot.services;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiChatbotService {
    private static final String MODEL = "gemini-2.5-flash";
    private static final Logger logger = LoggerFactory.getLogger(AiChatbotService.class);
    private final String apiKey;

    public AiChatbotService(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAiAnswer(String question) {
        try (Client client = new Client()) {
            System.setProperty("GEMINI_API_KEY", apiKey);
            GenerateContentResponse response = client.models.generateContent(
                MODEL,
                question,
                null
            );
            String aiText = response.text();
            logger.debug("[AI DEBUG] Gemini SDK response: {}", aiText);
            return aiText != null ? aiText.trim() : null;
        } catch (Exception e) {
            logger.error("[AI ERROR] {}", e.getMessage(), e);
        }
        return null;
    }
}
