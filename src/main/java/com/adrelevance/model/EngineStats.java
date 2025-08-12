package com.adrelevance.model;

/**
 * Represents engine statistics
 */
public class EngineStats {
    private int activeConversations;
    private int adInventorySize;
    private int totalUsers;

    public EngineStats(int activeConversations, int adInventorySize, int totalUsers) {
        this.activeConversations = activeConversations;
        this.adInventorySize = adInventorySize;
        this.totalUsers = totalUsers;
    }

    // Getters and Setters
    public int getActiveConversations() { return activeConversations; }
    public void setActiveConversations(int activeConversations) { this.activeConversations = activeConversations; }

    public int getAdInventorySize() { return adInventorySize; }
    public void setAdInventorySize(int adInventorySize) { this.adInventorySize = adInventorySize; }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    @Override
    public String toString() {
        return "EngineStats{" +
                "activeConversations=" + activeConversations +
                ", adInventorySize=" + adInventorySize +
                ", totalUsers=" + totalUsers +
                '}';
    }
}
