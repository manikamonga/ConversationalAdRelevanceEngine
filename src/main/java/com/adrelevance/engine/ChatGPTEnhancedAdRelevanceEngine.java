package com.adrelevance.engine;

import com.adrelevance.llm.ChatGPTService;
import com.adrelevance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced Ad Relevance Engine that uses ChatGPT for intelligent context analysis
 */
@Component
public class ChatGPTEnhancedAdRelevanceEngine {
    private static final Logger logger = LoggerFactory.getLogger(ChatGPTEnhancedAdRelevanceEngine.class);
    
    private final ChatGPTService chatGPTService;
    private final ConversationManager conversationManager;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, ConversationContext> conversationCache;
    
    @Autowired
    public ChatGPTEnhancedAdRelevanceEngine(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
        this.conversationManager = new ConversationManager();
        this.executorService = Executors.newFixedThreadPool(10);
        this.conversationCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Processes a message using ChatGPT for intelligent ad suggestions
     */
    public CompletableFuture<AdSuggestion> processMessage(String conversationId, String userId, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                // Get or create conversation context
                ConversationContext context = getOrCreateConversationContext(conversationId, userId);
                
                // Add user message to context
                Message userMessage = new Message(message, userId, MessageType.USER_MESSAGE);
                context.addMessage(userMessage);
                
                // Get conversation history for context
                List<Message> conversationHistory = context.getMessages();
                UserState userState = context.getUserState();
                
                // Use ChatGPT to analyze context and suggest ads
                ChatGPTService.ChatGPTAdSuggestion chatGPTSuggestion = chatGPTService
                    .analyzeContextAndSuggestAd(conversationId, userId, message, conversationHistory, userState)
                    .join();
                
                long processingTime = System.currentTimeMillis() - startTime;
                
                if (chatGPTSuggestion.hasAd()) {
                    // Create ad suggestion response
                    AdSuggestion adSuggestion = new AdSuggestion(
                        chatGPTSuggestion.getAd(),
                        chatGPTSuggestion.getAd().getConversationalTemplate(),
                        chatGPTSuggestion.getConfidence()
                    );
                    
                    // Add assistant response to context
                    Message assistantMessage = new Message(adSuggestion.getResponse(), "assistant", MessageType.BOT_RESPONSE);
                    context.addMessage(assistantMessage);
                    
                    logger.info("Generated ChatGPT-powered ad suggestion for conversation {}: {} (score: {}) in {}ms", 
                        conversationId, chatGPTSuggestion.getAd().getTitle(), chatGPTSuggestion.getConfidence(), processingTime);
                    
                    return adSuggestion;
                } else {
                    // No relevant ad found
                    String fallbackResponse = "I don't have ad suggestions for this product right now. Try asking about technology, fashion, travel, food, fitness, or beauty products! 💡";
                    
                    // Add assistant response to context
                    Message assistantMessage = new Message(fallbackResponse, "assistant", MessageType.BOT_RESPONSE);
                    context.addMessage(assistantMessage);
                    
                    logger.info("No relevant ad found for conversation {} (confidence: {}) in {}ms", 
                        conversationId, chatGPTSuggestion.getConfidence(), processingTime);
                    
                    return new AdSuggestion(null, fallbackResponse, 0.0);
                }
                
            } catch (Exception e) {
                logger.error("Error processing message with ChatGPT for conversation {}: {}", conversationId, e.getMessage(), e);
                return new AdSuggestion(null, "Sorry, I encountered an error processing your message.", 0.0);
            }
        }, executorService);
    }
    
    /**
     * Updates user preferences
     */
    public void updateUserPreferences(String userId, List<String> interests, List<String> blockedCategories, boolean adPreferencesEnabled) {
        UserState userState = conversationManager.getOrCreateUserState(userId);
        userState.setInterests(interests);
        userState.setBlockedCategories(blockedCategories);
        userState.setAdPreferencesEnabled(adPreferencesEnabled);
        conversationManager.updateUserState(userId, userState);
        
        // Clear conversation cache to ensure fresh context
        conversationCache.clear();
        
        logger.info("Updated user preferences for user {}: interests={}, blocked={}, enabled={}", 
            userId, interests, blockedCategories, adPreferencesEnabled);
    }
    
    /**
     * Gets engine statistics
     */
    public EngineStats getStats() {
        return new EngineStats(
            conversationCache.size(),
            0, // No hardcoded inventory with ChatGPT
            conversationManager.getTotalUserCount()
        );
    }
    
    /**
     * Gets or creates a conversation context
     */
    private ConversationContext getOrCreateConversationContext(String conversationId, String userId) {
        return conversationCache.computeIfAbsent(conversationId, id -> {
            ConversationContext context = new ConversationContext(id, userId);
            context.setUserState(conversationManager.getOrCreateUserState(userId));
            return context;
        });
    }
    
    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
