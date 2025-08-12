package com.adrelevance.engine;

import com.adrelevance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

/**
 * Main engine that orchestrates context analysis, ad matching, and response generation
 * Optimized for low latency with caching and async processing
 */
public class ConversationalAdRelevanceEngine {
    private static final Logger logger = LoggerFactory.getLogger(ConversationalAdRelevanceEngine.class);
    
    private final ContextAnalyzer contextAnalyzer;
    private final AdMatchingEngine adMatchingEngine;
    private final ConversationalResponseGenerator responseGenerator;
    private final ConversationManager conversationManager;
    
    // Performance optimizations
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, AdSuggestion> suggestionCache;
    private final ConcurrentHashMap<String, Long> cacheTimestamps;
    private static final long CACHE_TTL_MS = 30000; // 30 seconds cache TTL
    private static final int MAX_CACHE_SIZE = 1000;

    public ConversationalAdRelevanceEngine() {
        this.contextAnalyzer = new ContextAnalyzer();
        this.adMatchingEngine = new AdMatchingEngine();
        this.responseGenerator = new ConversationalResponseGenerator();
        this.conversationManager = new ConversationManager();
        
        // Performance optimizations
        this.executorService = Executors.newFixedThreadPool(4);
        this.suggestionCache = new ConcurrentHashMap<>();
        this.cacheTimestamps = new ConcurrentHashMap<>();
    }

    /**
     * Processes a new message and returns relevant ad suggestions with low latency
     */
    public AdSuggestion processMessage(String conversationId, String userId, String messageContent) {
        long startTime = System.currentTimeMillis();
        logger.debug("Processing message for conversation {}: {}", conversationId, messageContent);

        // Check cache first for similar messages
        String cacheKey = generateCacheKey(conversationId, userId, messageContent);
        AdSuggestion cachedSuggestion = getCachedSuggestion(cacheKey);
        if (cachedSuggestion != null) {
            logger.debug("Cache hit for conversation {}", conversationId);
            return cachedSuggestion;
        }

        // Get or create conversation context
        ConversationContext context = conversationManager.getOrCreateContext(conversationId, userId);
        
        // Add the new message
        Message message = new Message(messageContent, userId, MessageType.USER_MESSAGE);
        context.addMessage(message);

        // Analyze context (optimized for speed)
        contextAnalyzer.analyzeContext(context);

        // Find relevant ads with optimized matching
        List<Ad> relevantAds = adMatchingEngine.findRelevantAds(context, 3);

        if (relevantAds.isEmpty()) {
            logger.debug("No relevant ads found for conversation {}", conversationId);
            AdSuggestion suggestion = new AdSuggestion(null, "I'm here to help! What are you interested in today? 🤔", 0.0);
            cacheSuggestion(cacheKey, suggestion);
            return suggestion;
        }

        // Select the best ad
        Ad bestAd = relevantAds.get(0);
        
        // Generate conversational response
        String response = responseGenerator.generateResponse(bestAd, context);

        // Create ad suggestion
        AdSuggestion suggestion = new AdSuggestion(bestAd, response, bestAd.getRelevanceScore());

        // Cache the result
        cacheSuggestion(cacheKey, suggestion);

        long endTime = System.currentTimeMillis();
        logger.info("Generated ad suggestion for conversation {}: {} (score: {}) in {}ms", 
                   conversationId, bestAd.getTitle(), bestAd.getRelevanceScore(), (endTime - startTime));

        return suggestion;
    }

    /**
     * Async version for non-blocking processing
     */
    public CompletableFuture<AdSuggestion> processMessageAsync(String conversationId, String userId, String messageContent) {
        return CompletableFuture.supplyAsync(() -> 
            processMessage(conversationId, userId, messageContent), executorService);
    }

    /**
     * Processes user response to an ad suggestion with low latency
     */
    public String processAdResponse(String conversationId, String adId, String userResponse) {
        long startTime = System.currentTimeMillis();
        logger.debug("Processing ad response for conversation {}: {}", conversationId, userResponse);

        ConversationContext context = conversationManager.getContext(conversationId);
        if (context == null) {
            return "I'm not sure what you're referring to. Let's start a new conversation! 😊";
        }

        // Find the ad (optimized lookup)
        Optional<Ad> adOpt = adMatchingEngine.findRelevantAds(context, 10).stream()
                .filter(ad -> ad.getId().equals(adId))
                .findFirst();

        if (adOpt.isEmpty()) {
            return "I can't find that specific ad, but I'd love to help you find something else! 🤔";
        }

        Ad ad = adOpt.get();

        // Record user interaction
        if (context.getUserState() != null) {
            context.getUserState().recordAdInteraction(adId);
        }

        // Generate follow-up response
        String followUpResponse = responseGenerator.generateFollowUpResponse(ad, context, userResponse);

        // Add bot response to conversation
        Message botMessage = new Message(followUpResponse, "bot", MessageType.BOT_RESPONSE);
        context.addMessage(botMessage);

        long endTime = System.currentTimeMillis();
        logger.debug("Processed ad response in {}ms", (endTime - startTime));

        return followUpResponse;
    }

    /**
     * Gets conversation analytics with caching
     */
    public ConversationAnalytics getAnalytics(String conversationId) {
        ConversationContext context = conversationManager.getContext(conversationId);
        if (context == null) {
            return null;
        }

        return new ConversationAnalytics(
            context.getConversationId(),
            context.getMessages() != null ? context.getMessages().size() : 0,
            context.getMood(),
            context.getDetectedIntents(),
            context.getTopicWeights()
        );
    }

    /**
     * Updates user preferences with optimized storage
     */
    public void updateUserPreferences(String userId, List<String> interests, List<String> blockedCategories) {
        UserState userState = conversationManager.getOrCreateUserState(userId);
        
        if (interests != null) {
            userState.setInterests(new java.util.ArrayList<>(interests));
        }
        
        if (blockedCategories != null) {
            userState.setBlockedCategories(new java.util.ArrayList<>(blockedCategories));
        }

        // Clear related caches
        clearUserRelatedCaches(userId);

        logger.info("Updated user preferences for user {}: interests={}, blocked={}", 
                   userId, interests, blockedCategories);
    }

    /**
     * Adds a new ad to the inventory with cache invalidation
     */
    public void addAd(Ad ad) {
        adMatchingEngine.addAd(ad);
        // Clear suggestion cache since new ads are available
        clearSuggestionCache();
        logger.info("Added new ad to inventory: {}", ad.getId());
    }

    /**
     * Gets the current conversation context
     */
    public ConversationContext getConversationContext(String conversationId) {
        return conversationManager.getContext(conversationId);
    }

    /**
     * Clears a conversation context and related caches
     */
    public void clearConversation(String conversationId) {
        conversationManager.clearConversation(conversationId);
        clearConversationRelatedCaches(conversationId);
        logger.info("Cleared conversation: {}", conversationId);
    }

    /**
     * Gets engine statistics including performance metrics
     */
    public EngineStats getStats() {
        return new EngineStats(
            conversationManager.getActiveConversationCount(),
            adMatchingEngine.getInventorySize(),
            conversationManager.getTotalUserCount()
        );
    }

    /**
     * Shutdown the engine and cleanup resources
     */
    public void shutdown() {
        executorService.shutdown();
        suggestionCache.clear();
        cacheTimestamps.clear();
        logger.info("Engine shutdown completed");
    }

    // Performance optimization methods

    private String generateCacheKey(String conversationId, String userId, String messageContent) {
        // Simple hash-based cache key for performance
        return conversationId + ":" + userId + ":" + messageContent.hashCode();
    }

    private AdSuggestion getCachedSuggestion(String cacheKey) {
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < CACHE_TTL_MS) {
            return suggestionCache.get(cacheKey);
        }
        return null;
    }

    private void cacheSuggestion(String cacheKey, AdSuggestion suggestion) {
        // Implement LRU-like cache eviction
        if (suggestionCache.size() >= MAX_CACHE_SIZE) {
            // Simple eviction: remove oldest entries
            long oldestTime = System.currentTimeMillis();
            String oldestKey = null;
            
            for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
                if (entry.getValue() < oldestTime) {
                    oldestTime = entry.getValue();
                    oldestKey = entry.getKey();
                }
            }
            
            if (oldestKey != null) {
                suggestionCache.remove(oldestKey);
                cacheTimestamps.remove(oldestKey);
            }
        }
        
        suggestionCache.put(cacheKey, suggestion);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis());
    }

    private void clearSuggestionCache() {
        suggestionCache.clear();
        cacheTimestamps.clear();
    }

    private void clearUserRelatedCaches(String userId) {
        // Remove cache entries related to this user
        suggestionCache.entrySet().removeIf(entry -> 
            entry.getKey().contains(userId));
        cacheTimestamps.entrySet().removeIf(entry -> 
            entry.getKey().contains(userId));
    }

    private void clearConversationRelatedCaches(String conversationId) {
        // Remove cache entries related to this conversation
        suggestionCache.entrySet().removeIf(entry -> 
            entry.getKey().contains(conversationId));
        cacheTimestamps.entrySet().removeIf(entry -> 
            entry.getKey().contains(conversationId));
    }
}
