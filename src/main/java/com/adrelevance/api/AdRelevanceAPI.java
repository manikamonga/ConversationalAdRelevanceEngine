package com.adrelevance.api;

import com.adrelevance.engine.ConversationalAdRelevanceEngine;
import com.adrelevance.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for the Conversational Ad Relevance Engine
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AdRelevanceAPI {
    
    private static final Logger logger = LoggerFactory.getLogger(AdRelevanceAPI.class);
    
    private final ConversationalAdRelevanceEngine engine;
    private final ObjectMapper objectMapper;
    
    public AdRelevanceAPI() {
        this.engine = new ConversationalAdRelevanceEngine();
        this.objectMapper = new ObjectMapper();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(AdRelevanceAPI.class, args);
        logger.info("🚀 Ad Relevance API started on port 8080");
    }
    
    @PostMapping("/process-message")
    public ResponseEntity<Map<String, Object>> processMessage(@RequestBody Map<String, Object> request) {
        try {
            String conversationId = (String) request.get("conversationId");
            String userId = (String) request.get("userId");
            String message = (String) request.get("message");
            
            logger.info("Processing message for conversation {}: {}", conversationId, message);
            
            // Process the message
            AdSuggestion suggestion = engine.processMessage(conversationId, userId, message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conversationId);
            response.put("userId", userId);
            response.put("message", message);
            
            if (suggestion != null && suggestion.getAd() != null) {
                Map<String, Object> adSuggestion = new HashMap<>();
                adSuggestion.put("ad", convertAdToMap(suggestion.getAd()));
                adSuggestion.put("response", suggestion.getResponse());
                adSuggestion.put("relevanceScore", suggestion.getRelevanceScore());
                response.put("adSuggestion", adSuggestion);
            } else {
                response.put("adSuggestion", null);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing message", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @PostMapping("/update-preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            @SuppressWarnings("unchecked")
            List<String> interests = (List<String>) request.get("interests");
            @SuppressWarnings("unchecked")
            List<String> blockedCategories = (List<String>) request.get("blockedCategories");
            
            logger.info("Updating preferences for user {}: interests={}, blocked={}", 
                       userId, interests, blockedCategories);
            
            engine.updateUserPreferences(userId, interests, blockedCategories);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("message", "Preferences updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating preferences", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            EngineStats stats = engine.getStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("activeConversations", stats.getActiveConversations());
            response.put("adInventorySize", stats.getAdInventorySize());
            response.put("totalUsers", stats.getTotalUsers());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting stats", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Ad Relevance Engine API");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> convertAdToMap(Ad ad) {
        Map<String, Object> adMap = new HashMap<>();
        adMap.put("id", ad.getId());
        adMap.put("title", ad.getTitle());
        adMap.put("description", ad.getDescription());
        adMap.put("brandName", ad.getBrandName());
        adMap.put("categories", ad.getCategories());
        adMap.put("keywords", ad.getKeywords());
        adMap.put("callToAction", ad.getCallToAction());
        adMap.put("type", ad.getType().toString());
        return adMap;
    }
}
