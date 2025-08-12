package com.adrelevance.model;

import java.util.List;
import java.util.Map;

/**
 * Represents analytics data for a conversation
 */
public class ConversationAnalytics {
    private String conversationId;
    private int messageCount;
    private ConversationMood mood;
    private List<String> detectedIntents;
    private Map<String, Double> topicWeights;

    public ConversationAnalytics(String conversationId, int messageCount, ConversationMood mood, 
                                List<String> detectedIntents, Map<String, Double> topicWeights) {
        this.conversationId = conversationId;
        this.messageCount = messageCount;
        this.mood = mood;
        this.detectedIntents = detectedIntents;
        this.topicWeights = topicWeights;
    }

    // Getters and Setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

    public ConversationMood getMood() { return mood; }
    public void setMood(ConversationMood mood) { this.mood = mood; }

    public List<String> getDetectedIntents() { return detectedIntents; }
    public void setDetectedIntents(List<String> detectedIntents) { this.detectedIntents = detectedIntents; }

    public Map<String, Double> getTopicWeights() { return topicWeights; }
    public void setTopicWeights(Map<String, Double> topicWeights) { this.topicWeights = topicWeights; }

    @Override
    public String toString() {
        return "ConversationAnalytics{" +
                "conversationId='" + conversationId + '\'' +
                ", messageCount=" + messageCount +
                ", mood=" + mood +
                ", detectedIntents=" + detectedIntents +
                ", topicWeights=" + topicWeights +
                '}';
    }
}
