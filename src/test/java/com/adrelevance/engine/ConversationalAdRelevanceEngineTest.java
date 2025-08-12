package com.adrelevance.engine;

import com.adrelevance.model.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Basic tests for the ConversationalAdRelevanceEngine
 */
public class ConversationalAdRelevanceEngineTest {
    
    private ConversationalAdRelevanceEngine engine;
    
    @Before
    public void setUp() {
        engine = new ConversationalAdRelevanceEngine();
    }
    
    @Test
    public void testBasicMessageProcessing() {
        // Set up user preferences
        engine.updateUserPreferences("test_user", 
            Arrays.asList("technology", "fashion"), 
            Arrays.asList("gambling"));
        
        // Process a technology-related message
        AdSuggestion suggestion = engine.processMessage("test_conv", "test_user", 
            "I need a new smartphone");
        
        // Verify we got a response
        assertNotNull("AdSuggestion should not be null", suggestion);
        assertNotNull("Response should not be null", suggestion.getResponse());
        assertTrue("Response should not be empty", !suggestion.getResponse().trim().isEmpty());
        
        // If an ad was found, verify it has reasonable relevance
        if (suggestion.getAd() != null) {
            assertTrue("Relevance score should be positive", suggestion.getRelevanceScore() > 0);
            assertTrue("Relevance score should be reasonable", suggestion.getRelevanceScore() <= 1.0);
        }
    }
    
    @Test
    public void testFashionConversation() {
        engine.updateUserPreferences("fashion_user", 
            Arrays.asList("fashion", "style"), 
            Arrays.asList());
        
        // Simulate fashion conversation
        AdSuggestion suggestion1 = engine.processMessage("fashion_conv", "fashion_user", 
            "I'm looking for summer clothes");
        AdSuggestion suggestion2 = engine.processMessage("fashion_conv", "fashion_user", 
            "I love trendy styles");
        
        assertNotNull("First suggestion should not be null", suggestion1);
        assertNotNull("Second suggestion should not be null", suggestion2);
    }
    
    @Test
    public void testTravelConversation() {
        engine.updateUserPreferences("travel_user", 
            Arrays.asList("travel", "vacation"), 
            Arrays.asList());
        
        // Simulate travel conversation
        AdSuggestion suggestion = engine.processMessage("travel_conv", "travel_user", 
            "I'm planning a vacation to somewhere amazing");
        
        assertNotNull("Travel suggestion should not be null", suggestion);
        assertTrue("Response should contain travel-related content", 
            suggestion.getResponse().toLowerCase().contains("vacation") || 
            suggestion.getResponse().toLowerCase().contains("travel") ||
            suggestion.getResponse().toLowerCase().contains("trip"));
    }
    
    @Test
    public void testAdResponseProcessing() {
        engine.updateUserPreferences("test_user", Arrays.asList("technology"), Arrays.asList());
        
        // Get an ad suggestion first
        AdSuggestion suggestion = engine.processMessage("test_conv", "test_user", 
            "I need a new phone");
        
        if (suggestion.getAd() != null) {
            // Test positive response
            String positiveResponse = engine.processAdResponse("test_conv", 
                suggestion.getAd().getId(), "Yes, that looks great!");
            
            assertNotNull("Positive response should not be null", positiveResponse);
            assertTrue("Positive response should not be empty", !positiveResponse.trim().isEmpty());
            
            // Test negative response
            String negativeResponse = engine.processAdResponse("test_conv", 
                suggestion.getAd().getId(), "No, not interested");
            
            assertNotNull("Negative response should not be null", negativeResponse);
            assertTrue("Negative response should not be empty", !negativeResponse.trim().isEmpty());
        }
    }
    
    @Test
    public void testAnalytics() {
        engine.updateUserPreferences("analytics_user", Arrays.asList("technology"), Arrays.asList());
        
        // Process some messages
        engine.processMessage("analytics_conv", "analytics_user", "I need a smartphone");
        engine.processMessage("analytics_conv", "analytics_user", "I'm excited about new tech");
        
        // Get analytics
        ConversationAnalytics analytics = engine.getAnalytics("analytics_conv");
        
        assertNotNull("Analytics should not be null", analytics);
        assertEquals("Conversation ID should match", "analytics_conv", analytics.getConversationId());
        assertTrue("Should have at least 2 messages", analytics.getMessageCount() >= 2);
        assertNotNull("Mood should be detected", analytics.getMood());
    }
    
    @Test
    public void testEngineStats() {
        // Process some messages to generate activity
        engine.updateUserPreferences("stats_user", Arrays.asList("technology"), Arrays.asList());
        engine.processMessage("stats_conv1", "stats_user", "I need a phone");
        engine.processMessage("stats_conv2", "stats_user", "I love technology");
        
        EngineStats stats = engine.getStats();
        
        assertNotNull("Engine stats should not be null", stats);
        assertTrue("Should have active conversations", stats.getActiveConversations() > 0);
        assertTrue("Should have ad inventory", stats.getAdInventorySize() > 0);
        assertTrue("Should have users", stats.getTotalUsers() > 0);
    }
}
