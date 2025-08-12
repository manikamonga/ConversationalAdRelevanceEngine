package com.adrelevance.model;

/**
 * Represents an ad suggestion with conversational response and relevance score
 */
public class AdSuggestion {
    private Ad ad;
    private String response;
    private double relevanceScore;

    public AdSuggestion(Ad ad, String response, double relevanceScore) {
        this.ad = ad;
        this.response = response;
        this.relevanceScore = relevanceScore;
    }

    // Getters and Setters
    public Ad getAd() { return ad; }
    public void setAd(Ad ad) { this.ad = ad; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; }

    @Override
    public String toString() {
        return "AdSuggestion{" +
                "ad=" + (ad != null ? ad.getTitle() : "null") +
                ", response='" + response + '\'' +
                ", relevanceScore=" + relevanceScore +
                '}';
    }
}
