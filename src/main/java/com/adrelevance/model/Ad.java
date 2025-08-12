package com.adrelevance.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents an advertisement with targeting and conversational properties
 */
public class Ad {
    private String id;
    private String title;
    private String description;
    private String brandName;
    private String callToAction;
    private List<String> categories;
    private List<String> targetAudience;
    private Map<String, Double> topicRelevance;
    private Map<UserMood, Double> moodRelevance;
    private List<String> keywords;
    private String conversationalTemplate;
    private AdType type;
    private double relevanceScore;
    private boolean isActive;

    public Ad() {
        this.categories = new java.util.ArrayList<>();
        this.targetAudience = new java.util.ArrayList<>();
        this.topicRelevance = new HashMap<>();
        this.moodRelevance = new HashMap<>();
        this.keywords = new java.util.ArrayList<>();
        this.isActive = true;
    }

    public Ad(String id, String title, String description, String brandName) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.brandName = brandName;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getCallToAction() { return callToAction; }
    public void setCallToAction(String callToAction) { this.callToAction = callToAction; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public List<String> getTargetAudience() { return targetAudience; }
    public void setTargetAudience(List<String> targetAudience) { this.targetAudience = targetAudience; }

    public Map<String, Double> getTopicRelevance() { return topicRelevance; }
    public void setTopicRelevance(Map<String, Double> topicRelevance) { this.topicRelevance = topicRelevance; }

    public Map<UserMood, Double> getMoodRelevance() { return moodRelevance; }
    public void setMoodRelevance(Map<UserMood, Double> moodRelevance) { this.moodRelevance = moodRelevance; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getConversationalTemplate() { return conversationalTemplate; }
    public void setConversationalTemplate(String conversationalTemplate) { this.conversationalTemplate = conversationalTemplate; }

    public AdType getType() { return type; }
    public void setType(AdType type) { this.type = type; }

    public double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    public void addTargetAudience(String audience) {
        this.targetAudience.add(audience);
    }

    public void addKeyword(String keyword) {
        this.keywords.add(keyword);
    }

    public void setTopicRelevance(String topic, Double relevance) {
        this.topicRelevance.put(topic, relevance);
    }

    public void setMoodRelevance(UserMood mood, Double relevance) {
        this.moodRelevance.put(mood, relevance);
    }

    @Override
    public String toString() {
        return "Ad{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", brandName='" + brandName + '\'' +
                ", categories=" + categories +
                ", relevanceScore=" + relevanceScore +
                ", isActive=" + isActive +
                '}';
    }
}
