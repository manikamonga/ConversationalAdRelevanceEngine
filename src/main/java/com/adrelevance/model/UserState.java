package com.adrelevance.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents the current state and preferences of a user
 */
public class UserState {
    private String userId;
    private List<String> interests;
    private Map<String, Double> topicPreferences;
    private UserMood currentMood;
    private List<String> recentSearches;
    private Map<String, Integer> adInteractionHistory;
    private String preferredLanguage;
    private boolean adPreferencesEnabled;
    private List<String> blockedCategories;

    public UserState() {
        this.topicPreferences = new HashMap<>();
        this.recentSearches = new java.util.ArrayList<>();
        this.adInteractionHistory = new HashMap<>();
        this.interests = new java.util.ArrayList<>();
        this.blockedCategories = new java.util.ArrayList<>();
    }

    public UserState(String userId) {
        this();
        this.userId = userId;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public Map<String, Double> getTopicPreferences() { return topicPreferences; }
    public void setTopicPreferences(Map<String, Double> topicPreferences) { this.topicPreferences = topicPreferences; }

    public UserMood getCurrentMood() { return currentMood; }
    public void setCurrentMood(UserMood currentMood) { this.currentMood = currentMood; }

    public List<String> getRecentSearches() { return recentSearches; }
    public void setRecentSearches(List<String> recentSearches) { this.recentSearches = recentSearches; }

    public Map<String, Integer> getAdInteractionHistory() { return adInteractionHistory; }
    public void setAdInteractionHistory(Map<String, Integer> adInteractionHistory) { this.adInteractionHistory = adInteractionHistory; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public boolean isAdPreferencesEnabled() { return adPreferencesEnabled; }
    public void setAdPreferencesEnabled(boolean adPreferencesEnabled) { this.adPreferencesEnabled = adPreferencesEnabled; }

    public List<String> getBlockedCategories() { return blockedCategories; }
    public void setBlockedCategories(List<String> blockedCategories) { this.blockedCategories = blockedCategories; }

    public void addInterest(String interest) {
        if (this.interests == null) {
            this.interests = new java.util.ArrayList<>();
        }
        this.interests.add(interest);
    }

    public void setTopicPreference(String topic, Double preference) {
        this.topicPreferences.put(topic, preference);
    }

    public void addRecentSearch(String search) {
        this.recentSearches.add(search);
        if (this.recentSearches.size() > 10) {
            this.recentSearches.remove(0);
        }
    }

    public void recordAdInteraction(String adId) {
        this.adInteractionHistory.put(adId, this.adInteractionHistory.getOrDefault(adId, 0) + 1);
    }

    @Override
    public String toString() {
        return "UserState{" +
                "userId='" + userId + '\'' +
                ", interests=" + interests +
                ", currentMood=" + currentMood +
                ", adPreferencesEnabled=" + adPreferencesEnabled +
                '}';
    }
}
