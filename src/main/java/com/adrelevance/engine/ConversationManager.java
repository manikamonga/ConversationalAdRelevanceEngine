package com.adrelevance.engine;

import com.adrelevance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages conversation contexts and user states
 */
public class ConversationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConversationManager.class);
    
    private final Map<String, ConversationContext> conversations;
    private final Map<String, UserState> userStates;

    public ConversationManager() {
        this.conversations = new ConcurrentHashMap<>();
        this.userStates = new ConcurrentHashMap<>();
    }

    /**
     * Gets or creates a conversation context
     */
    public ConversationContext getOrCreateContext(String conversationId, String userId) {
        return conversations.computeIfAbsent(conversationId, id -> {
            ConversationContext context = new ConversationContext(id, userId);
            UserState userState = getOrCreateUserState(userId);
            context.setUserState(userState);
            logger.info("Created new conversation context: {}", id);
            return context;
        });
    }

    /**
     * Gets an existing conversation context
     */
    public ConversationContext getContext(String conversationId) {
        return conversations.get(conversationId);
    }

    /**
     * Gets or creates a user state
     */
    public UserState getOrCreateUserState(String userId) {
        return userStates.computeIfAbsent(userId, id -> {
            UserState userState = new UserState(id);
            logger.info("Created new user state: {}", id);
            return userState;
        });
    }

    /**
     * Gets an existing user state
     */
    public UserState getUserState(String userId) {
        return userStates.get(userId);
    }

    /**
     * Clears a conversation context
     */
    public void clearConversation(String conversationId) {
        conversations.remove(conversationId);
        logger.info("Cleared conversation: {}", conversationId);
    }

    /**
     * Gets the number of active conversations
     */
    public int getActiveConversationCount() {
        return conversations.size();
    }

    /**
     * Gets the total number of users
     */
    public int getTotalUserCount() {
        return userStates.size();
    }

    /**
     * Updates user state
     */
    public void updateUserState(String userId, UserState userState) {
        userStates.put(userId, userState);
        logger.info("Updated user state: {}", userId);
    }

    /**
     * Removes a user state
     */
    public void removeUserState(String userId) {
        userStates.remove(userId);
        logger.info("Removed user state: {}", userId);
    }
}
