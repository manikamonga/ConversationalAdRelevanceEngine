package com.adrelevance.model;

import java.time.LocalDateTime;

/**
 * Represents a single message in a conversation
 */
public class Message {
    private String id;
    private String content;
    private String senderId;
    private MessageType type;
    private LocalDateTime timestamp;
    private double sentimentScore;
    private String detectedLanguage;

    public Message() {
        this.timestamp = LocalDateTime.now();
    }

    public Message(String content, String senderId, MessageType type) {
        this();
        this.content = content;
        this.senderId = senderId;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }

    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", senderId='" + senderId + '\'' +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", sentimentScore=" + sentimentScore +
                '}';
    }
}
