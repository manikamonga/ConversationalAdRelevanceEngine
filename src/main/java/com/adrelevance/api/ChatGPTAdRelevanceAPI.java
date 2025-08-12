package com.adrelevance.api;

import com.adrelevance.engine.ChatGPTEnhancedAdRelevanceEngine;
import com.adrelevance.model.AdSuggestion;
import com.adrelevance.model.EngineStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Spring Boot REST API for ChatGPT-enhanced Conversational Ad Relevance Engine
 */
@SpringBootApplication(scanBasePackages = "com.adrelevance")
@RestController
@RequestMapping("/api/chatgpt")
@CrossOrigin(origins = "*")
public class ChatGPTAdRelevanceAPI {
    private static final Logger logger = LoggerFactory.getLogger(ChatGPTAdRelevanceAPI.class);
    
    @Autowired
    private ChatGPTEnhancedAdRelevanceEngine chatGPTEngine;
    
    public static void main(String[] args) {
        SpringApplication.run(ChatGPTAdRelevanceAPI.class, args);
        logger.info("🚀 ChatGPT-Enhanced Ad Relevance API started on port 8080");
    }
    
    /**
     * Process a message using ChatGPT for intelligent ad suggestions
     */
    @PostMapping("/process-message")
    public CompletableFuture<Map<String, Object>> processMessage(@RequestBody Map<String, Object> request) {
        String conversationId = (String) request.get("conversationId");
        String userId = (String) request.get("userId");
        String message = (String) request.get("message");
        
        logger.info("Processing message with ChatGPT for conversation {}: {}", conversationId, message);
        
        return chatGPTEngine.processMessage(conversationId, userId, message)
            .thenApply(adSuggestion -> {
                Map<String, Object> response = new HashMap<>();
                response.put("conversationId", conversationId);
                response.put("message", message);
                response.put("userId", userId);
                
                if (adSuggestion != null && adSuggestion.getAd() != null) {
                    Map<String, Object> adSuggestionMap = new HashMap<>();
                    adSuggestionMap.put("ad", convertAdToMap(adSuggestion.getAd()));
                    adSuggestionMap.put("response", adSuggestion.getResponse());
                    adSuggestionMap.put("relevanceScore", adSuggestion.getRelevanceScore());
                    response.put("adSuggestion", adSuggestionMap);
                } else {
                    response.put("adSuggestion", null);
                }
                
                return response;
            });
    }
    
    /**
     * Update user preferences
     */
    @PostMapping("/update-preferences")
    public Map<String, Object> updatePreferences(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        @SuppressWarnings("unchecked")
        List<String> interests = (List<String>) request.get("interests");
        @SuppressWarnings("unchecked")
        List<String> blockedCategories = (List<String>) request.get("blockedCategories");
        Boolean adPreferencesEnabled = (Boolean) request.get("adPreferencesEnabled");
        
        chatGPTEngine.updateUserPreferences(userId, interests, blockedCategories, adPreferencesEnabled);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User preferences updated successfully");
        response.put("userId", userId);
        
        return response;
    }
    
    /**
     * Get engine statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        EngineStats stats = chatGPTEngine.getStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("activeConversations", stats.getActiveConversations());
        response.put("adInventorySize", stats.getAdInventorySize());
        response.put("totalUsers", stats.getTotalUsers());
        response.put("timestamp", System.currentTimeMillis());
        response.put("engine", "ChatGPT-Enhanced");
        
        return response;
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("engine", "ChatGPT-Enhanced Ad Relevance Engine");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * Convert Ad object to Map for JSON serialization
     */
    private Map<String, Object> convertAdToMap(com.adrelevance.model.Ad ad) {
        Map<String, Object> adMap = new HashMap<>();
        adMap.put("id", ad.getId());
        adMap.put("title", ad.getTitle());
        adMap.put("description", ad.getDescription());
        adMap.put("brandName", ad.getBrandName());
        adMap.put("categories", ad.getCategories());
        adMap.put("keywords", ad.getKeywords());
        adMap.put("callToAction", ad.getCallToAction());
        adMap.put("type", ad.getType());
        adMap.put("conversationalTemplate", ad.getConversationalTemplate());
        return adMap;
    }
}
