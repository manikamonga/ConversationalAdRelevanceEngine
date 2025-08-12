package com.adrelevance.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents the context of an ongoing conversation
 */
public class ConversationContext {
    private String conversationId;
    private String userId;
    private List<Message> messages;
    private UserState userState;
    private Map<String, Object> metadata;
    private LocalDateTime lastActivity;
    private ConversationMood mood;
    private List<String> detectedIntents;
    private Map<String, Double> topicWeights;

    public ConversationContext() {
        this.metadata = new HashMap<>();
        this.detectedIntents = new java.util.ArrayList<>();
        this.topicWeights = new HashMap<>();
    }

    public ConversationContext(String conversationId, String userId) {
        this();
        this.conversationId = conversationId;
        this.userId = userId;
        this.lastActivity = LocalDateTime.now();
    }

    // Getters and Setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public UserState getUserState() { return userState; }
    public void setUserState(UserState userState) { this.userState = userState; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public ConversationMood getMood() { return mood; }
    public void setMood(ConversationMood mood) { this.mood = mood; }

    public List<String> getDetectedIntents() { return detectedIntents; }
    public void setDetectedIntents(List<String> detectedIntents) { this.detectedIntents = detectedIntents; }

    public Map<String, Double> getTopicWeights() { return topicWeights; }
    public void setTopicWeights(Map<String, Double> topicWeights) { this.topicWeights = topicWeights; }

    public void addMessage(Message message) {
        if (this.messages == null) {
            this.messages = new java.util.ArrayList<>();
        }
        this.messages.add(message);
        this.lastActivity = LocalDateTime.now();
    }

    public void addIntent(String intent) {
        this.detectedIntents.add(intent);
    }

    public void setTopicWeight(String topic, Double weight) {
        this.topicWeights.put(topic, weight);
    }

    @Override
    public String toString() {
        return "ConversationContext{" +
                "conversationId='" + conversationId + '\'' +
                ", userId='" + userId + '\'' +
                ", messageCount=" + (messages != null ? messages.size() : 0) +
                ", mood=" + mood +
                ", intents=" + detectedIntents +
                ", lastActivity=" + lastActivity +
                '}';
    }
}
