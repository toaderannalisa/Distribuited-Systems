package com.example.chatbot.services;



import java.util.HashMap;
import java.util.Map;

public class RuleBasedChatbotService {
    private final Map<String, String> rules = new HashMap<>();

    public RuleBasedChatbotService() {
        rules.put("hello", "Hello! How can I help you?");
        rules.put("hi", "Hi! How can I assist you?");
        rules.put("how are you", "I'm just a bot, but I'm here to help!");
        rules.put("support", "For support, please describe your issue.");
        rules.put("problem", "Can you give more details about your problem?");
        rules.put("max consumption", "For max consumption alerts, please check your device consumption .");
        rules.put("contact", "You can contact us at support@example.com.");
        rules.put("device", "For device issues, try restarting your device first.");
        rules.put("bye", "Goodbye! Have a great day.");
        rules.put("thank you", "You're welcome! If you have more questions, ask anytime.");
    }

    public String getAnswer(String question) {
        String q = question.toLowerCase();
        for (String key : rules.keySet()) {
            if (q.contains(key)) {
                return rules.get(key);
            }
        }
        return "Sorry, I don't understand. Can you rephrase?";
    }
}
