package com.adrelevance.demo;

import com.adrelevance.engine.ConversationalAdRelevanceEngine;
import com.adrelevance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Demo application showcasing the Conversational Ad Relevance Engine
 */
public class ConversationalAdDemo {
    private static final Logger logger = LoggerFactory.getLogger(ConversationalAdDemo.class);
    
    private final ConversationalAdRelevanceEngine engine;
    private final Scanner scanner;

    public ConversationalAdDemo() {
        this.engine = new ConversationalAdRelevanceEngine();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("🎯 Welcome to the Context-Aware Conversational Ad Relevance Engine! 🎯");
        System.out.println("This demo simulates conversations and shows how ads are matched based on context.\n");

        // Set up some user preferences
        setupUserPreferences();

        // Run different conversation scenarios
        runFashionScenario();
        runTechnologyScenario();
        runTravelScenario();
        runInteractiveMode();

        System.out.println("\n🎉 Demo completed! Thanks for exploring the Conversational Ad Relevance Engine!");
    }

    private void setupUserPreferences() {
        System.out.println("📝 Setting up user preferences...");
        
        // Create a user with some interests
        String userId = "demo_user_001";
        List<String> interests = Arrays.asList("technology", "fashion", "travel", "food");
        List<String> blockedCategories = Arrays.asList("gambling", "alcohol");
        
        engine.updateUserPreferences(userId, interests, blockedCategories);
        System.out.println("✅ User preferences set: " + interests);
    }

    private void runFashionScenario() {
        System.out.println("\n👗 === FASHION CONVERSATION SCENARIO ===");
        
        String conversationId = "fashion_conv_001";
        String userId = "demo_user_001";
        
        // Simulate a fashion-related conversation
        String[] messages = {
            "I'm looking for some new summer clothes",
            "I love trendy styles and bright colors",
            "Do you have any recommendations for summer fashion?",
            "I'm so excited about the new season!"
        };

        simulateConversation(conversationId, userId, messages);
    }

    private void runTechnologyScenario() {
        System.out.println("\n📱 === TECHNOLOGY CONVERSATION SCENARIO ===");
        
        String conversationId = "tech_conv_001";
        String userId = "demo_user_001";
        
        // Simulate a technology-related conversation
        String[] messages = {
            "I need a new smartphone",
            "I'm researching the latest features",
            "What's the best phone for photography?",
            "I'm curious about the new AI features"
        };

        simulateConversation(conversationId, userId, messages);
    }

    private void runTravelScenario() {
        System.out.println("\n✈️ === TRAVEL CONVERSATION SCENARIO ===");
        
        String conversationId = "travel_conv_001";
        String userId = "demo_user_001";
        
        // Simulate a travel-related conversation
        String[] messages = {
            "I'm planning a vacation",
            "I want to go somewhere amazing",
            "Looking for travel deals and destinations",
            "I'm so excited about my upcoming trip!"
        };

        simulateConversation(conversationId, userId, messages);
    }

    private void simulateConversation(String conversationId, String userId, String[] messages) {
        System.out.println("💬 Simulating conversation...");
        
        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            System.out.println("\n👤 User: " + message);
            
            // Process message and get ad suggestion
            AdSuggestion suggestion = engine.processMessage(conversationId, userId, message);
            
            System.out.println("🤖 Bot: " + suggestion.getResponse());
            
            if (suggestion.getAd() != null) {
                System.out.println("📊 Ad: " + suggestion.getAd().getTitle() + 
                                 " (Relevance: " + String.format("%.2f", suggestion.getRelevanceScore()) + ")");
                
                // Simulate user response to ad
                if (i == messages.length - 1) { // Only on last message
                    simulateAdResponse(conversationId, suggestion.getAd().getId());
                }
            }
            
            // Add some delay for readability
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Show analytics
        showConversationAnalytics(conversationId);
    }

    private void simulateAdResponse(String conversationId, String adId) {
        System.out.println("\n👤 User: That looks interesting! Tell me more.");
        
        String response = engine.processAdResponse(conversationId, adId, "That looks interesting! Tell me more.");
        System.out.println("🤖 Bot: " + response);
    }

    private void showConversationAnalytics(String conversationId) {
        ConversationAnalytics analytics = engine.getAnalytics(conversationId);
        if (analytics != null) {
            System.out.println("\n📈 === CONVERSATION ANALYTICS ===");
            System.out.println("Messages: " + analytics.getMessageCount());
            System.out.println("Mood: " + analytics.getMood());
            System.out.println("Detected Intents: " + analytics.getDetectedIntents());
            System.out.println("Topic Weights: " + analytics.getTopicWeights());
        }
    }

    private void runInteractiveMode() {
        System.out.println("\n🎮 === INTERACTIVE MODE ===");
        System.out.println("Now you can chat with the engine! Type 'quit' to exit.");
        
        String conversationId = "interactive_conv_001";
        String userId = "demo_user_001";
        
        while (true) {
            System.out.print("\n👤 You: ");
            String userInput = scanner.nextLine().trim();
            
            if ("quit".equalsIgnoreCase(userInput)) {
                break;
            }
            
            if (userInput.isEmpty()) {
                continue;
            }
            
            // Process message
            AdSuggestion suggestion = engine.processMessage(conversationId, userId, userInput);
            System.out.println("🤖 Bot: " + suggestion.getResponse());
            
            if (suggestion.getAd() != null) {
                System.out.println("📊 Ad: " + suggestion.getAd().getTitle() + 
                                 " (Relevance: " + String.format("%.2f", suggestion.getRelevanceScore()) + ")");
                
                System.out.print("💬 Respond to ad (yes/no/maybe): ");
                String adResponse = scanner.nextLine().trim();
                
                if (!adResponse.isEmpty()) {
                    String followUp = engine.processAdResponse(conversationId, suggestion.getAd().getId(), adResponse);
                    System.out.println("🤖 Bot: " + followUp);
                }
            }
        }
    }

    public static void main(String[] args) {
        ConversationalAdDemo demo = new ConversationalAdDemo();
        demo.run();
    }
}
