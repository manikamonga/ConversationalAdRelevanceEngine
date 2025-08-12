package com.adrelevance.engine;

import com.adrelevance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Analyzes conversation context to detect intent, mood, and topics
 */
public class ContextAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(ContextAnalyzer.class);
    
    private final Map<String, List<String>> intentKeywords;
    private final Map<String, List<String>> moodKeywords;
    private final Map<String, List<String>> topicKeywords;

    public ContextAnalyzer() {
        this.intentKeywords = initializeIntentKeywords();
        this.moodKeywords = initializeMoodKeywords();
        this.topicKeywords = initializeTopicKeywords();
    }

    /**
     * Analyzes the conversation context and updates it with detected information
     */
    public void analyzeContext(ConversationContext context) {
        if (context.getMessages() == null || context.getMessages().isEmpty()) {
            return;
        }

        // Analyze recent messages for intent and mood
        List<Message> recentMessages = getRecentMessages(context.getMessages(), 5);
        String combinedText = combineMessages(recentMessages);
        
        // Detect intents
        List<String> detectedIntents = detectIntents(combinedText);
        context.setDetectedIntents(detectedIntents);
        
        // Detect mood
        ConversationMood mood = detectConversationMood(combinedText);
        context.setMood(mood);
        
        // Extract topics
        Map<String, Double> topicWeights = extractTopics(combinedText);
        context.setTopicWeights(topicWeights);
        
        // Update user state if available
        if (context.getUserState() != null) {
            updateUserState(context.getUserState(), mood, detectedIntents);
        }

        logger.info("Context analysis completed for conversation {}: mood={}, intents={}", 
                   context.getConversationId(), mood, detectedIntents);
    }

    private List<Message> getRecentMessages(List<Message> messages, int count) {
        int startIndex = Math.max(0, messages.size() - count);
        return messages.subList(startIndex, messages.size());
    }

    private String combineMessages(List<Message> messages) {
        StringBuilder combined = new StringBuilder();
        for (Message message : messages) {
            if (message.getContent() != null) {
                combined.append(message.getContent()).append(" ");
            }
        }
        return combined.toString().toLowerCase().trim();
    }

    private List<String> detectIntents(String text) {
        List<String> detectedIntents = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : intentKeywords.entrySet()) {
            String intent = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (text.contains(keyword.toLowerCase())) {
                    detectedIntents.add(intent);
                    break;
                }
            }
        }
        
        return detectedIntents;
    }

    private ConversationMood detectConversationMood(String text) {
        Map<ConversationMood, Integer> moodScores = new HashMap<>();
        
        for (ConversationMood mood : ConversationMood.values()) {
            moodScores.put(mood, 0);
        }
        
        for (Map.Entry<String, List<String>> entry : moodKeywords.entrySet()) {
            String moodName = entry.getKey();
            List<String> keywords = entry.getValue();
            
            try {
                ConversationMood mood = ConversationMood.valueOf(moodName.toUpperCase());
                int score = 0;
                
                for (String keyword : keywords) {
                    if (text.contains(keyword.toLowerCase())) {
                        score++;
                    }
                }
                
                moodScores.put(mood, score);
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown mood: {}", moodName);
            }
        }
        
        return moodScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ConversationMood.NEUTRAL);
    }

    private Map<String, Double> extractTopics(String text) {
        Map<String, Double> topicWeights = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : topicKeywords.entrySet()) {
            String topic = entry.getKey();
            List<String> keywords = entry.getValue();
            
            double weight = 0.0;
            for (String keyword : keywords) {
                if (text.contains(keyword.toLowerCase())) {
                    weight += 1.0;
                }
            }
            
            if (weight > 0) {
                topicWeights.put(topic, weight);
            }
        }
        
        return topicWeights;
    }

    private void updateUserState(UserState userState, ConversationMood mood, List<String> intents) {
        // Map conversation mood to user mood
        UserMood userMood = mapConversationMoodToUserMood(mood);
        userState.setCurrentMood(userMood);
        
        // Add detected intents as interests if they're not already present
        for (String intent : intents) {
            if (!userState.getInterests().contains(intent)) {
                userState.addInterest(intent);
            }
        }
    }

    private UserMood mapConversationMoodToUserMood(ConversationMood conversationMood) {
        switch (conversationMood) {
            case POSITIVE: return UserMood.HAPPY;
            case EXCITED: return UserMood.EXCITED;
            case NEGATIVE: return UserMood.FRUSTRATED;
            case FRUSTRATED: return UserMood.FRUSTRATED;
            case CURIOUS: return UserMood.CURIOUS;
            case HUMOROUS: return UserMood.HAPPY;
            case SERIOUS: return UserMood.NEUTRAL;
            default: return UserMood.NEUTRAL;
        }
    }

    private Map<String, List<String>> initializeIntentKeywords() {
        Map<String, List<String>> keywords = new HashMap<>();
        
        keywords.put("shopping", Arrays.asList("buy", "purchase", "shop", "order", "shopping", "store"));
        keywords.put("research", Arrays.asList("research", "compare", "review", "information", "details"));
        keywords.put("entertainment", Arrays.asList("watch", "movie", "game", "music", "fun", "entertainment"));
        keywords.put("travel", Arrays.asList("travel", "trip", "vacation", "hotel", "flight", "destination"));
        keywords.put("food", Arrays.asList("food", "restaurant", "cook", "recipe", "dining", "meal"));
        keywords.put("health", Arrays.asList("health", "fitness", "exercise", "wellness", "medical"));
        keywords.put("technology", Arrays.asList("tech", "computer", "phone", "software", "app", "device"));
        
        return keywords;
    }

    private Map<String, List<String>> initializeMoodKeywords() {
        Map<String, List<String>> keywords = new HashMap<>();
        
        keywords.put("positive", Arrays.asList("great", "awesome", "amazing", "love", "happy", "excited"));
        keywords.put("negative", Arrays.asList("bad", "terrible", "hate", "angry", "frustrated", "disappointed"));
        keywords.put("excited", Arrays.asList("wow", "incredible", "fantastic", "thrilled", "excited"));
        keywords.put("frustrated", Arrays.asList("annoying", "frustrating", "difficult", "problem", "issue"));
        keywords.put("curious", Arrays.asList("wonder", "curious", "interesting", "tell me", "how"));
        keywords.put("humorous", Arrays.asList("funny", "joke", "hilarious", "lol", "haha"));
        keywords.put("serious", Arrays.asList("important", "serious", "critical", "urgent", "necessary"));
        
        return keywords;
    }

    private Map<String, List<String>> initializeTopicKeywords() {
        Map<String, List<String>> keywords = new HashMap<>();
        
        keywords.put("fashion", Arrays.asList("clothes", "fashion", "style", "outfit", "dress", "shoes"));
        keywords.put("electronics", Arrays.asList("phone", "computer", "laptop", "tablet", "electronics"));
        keywords.put("automotive", Arrays.asList("car", "vehicle", "automotive", "driving", "transport"));
        keywords.put("home", Arrays.asList("home", "house", "furniture", "decor", "kitchen"));
        keywords.put("sports", Arrays.asList("sports", "fitness", "exercise", "gym", "athletic"));
        keywords.put("beauty", Arrays.asList("beauty", "cosmetics", "skincare", "makeup", "personal care"));
        keywords.put("finance", Arrays.asList("money", "finance", "banking", "investment", "budget"));
        
        return keywords;
    }
}
