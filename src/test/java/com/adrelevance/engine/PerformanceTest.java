package com.adrelevance.engine;

import com.adrelevance.model.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Performance tests for the ConversationalAdRelevanceEngine
 */
public class PerformanceTest {
    
    @Test
    public void testLowLatencyProcessing() {
        ConversationalAdRelevanceEngine engine = new ConversationalAdRelevanceEngine();
        
        // Set up user preferences
        engine.updateUserPreferences("perf_user", 
            Arrays.asList("technology", "fashion", "travel"), 
            Arrays.asList("gambling"));
        
        String conversationId = "perf_conv";
        String userId = "perf_user";
        
        // Test single message processing latency
        long startTime = System.nanoTime();
        AdSuggestion suggestion = engine.processMessage(conversationId, userId, 
            "I need a new smartphone");
        long endTime = System.nanoTime();
        
        long latencyMs = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        System.out.println("Single message processing latency: " + latencyMs + "ms");
        
        // Assert low latency (should be under 100ms for typical use case)
        assertTrue("Latency should be under 100ms", latencyMs < 100);
        assertNotNull("Should return a suggestion", suggestion);
        
        // Test cache hit latency
        startTime = System.nanoTime();
        AdSuggestion cachedSuggestion = engine.processMessage(conversationId, userId, 
            "I need a new smartphone");
        endTime = System.nanoTime();
        
        long cacheLatencyMs = (endTime - startTime) / 1_000_000;
        System.out.println("Cache hit latency: " + cacheLatencyMs + "ms");
        
        // Cache hit should be much faster
        assertTrue("Cache hit should be under 10ms", cacheLatencyMs < 10);
        assertEquals("Cached suggestion should be the same", suggestion.getResponse(), cachedSuggestion.getResponse());
    }
    
    @Test
    public void testConcurrentProcessing() throws Exception {
        ConversationalAdRelevanceEngine engine = new ConversationalAdRelevanceEngine();
        
        // Set up user preferences
        engine.updateUserPreferences("concurrent_user", 
            Arrays.asList("technology", "fashion"), 
            Arrays.asList());
        
        String[] messages = {
            "I need a new smartphone",
            "I'm looking for summer clothes",
            "What's the best phone for photography?",
            "I love trendy styles",
            "I'm researching the latest features"
        };
        
        // Test concurrent processing
        long startTime = System.nanoTime();
        
        List<CompletableFuture<AdSuggestion>> futures = Arrays.stream(messages)
            .map(message -> engine.processMessageAsync("concurrent_conv", "concurrent_user", message))
            .collect(Collectors.toList());
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
        
        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Concurrent processing of " + messages.length + " messages: " + totalTimeMs + "ms");
        System.out.println("Average per message: " + (totalTimeMs / messages.length) + "ms");
        
        // Verify all suggestions were generated
        for (CompletableFuture<AdSuggestion> future : futures) {
            AdSuggestion suggestion = future.get();
            assertNotNull("Each message should generate a suggestion", suggestion);
            assertNotNull("Each suggestion should have a response", suggestion.getResponse());
        }
        
        // Concurrent processing should be efficient
        assertTrue("Total time should be reasonable", totalTimeMs < 1000);
    }
    
    @Test
    public void testHighThroughput() {
        ConversationalAdRelevanceEngine engine = new ConversationalAdRelevanceEngine();
        
        // Set up user preferences
        engine.updateUserPreferences("throughput_user", 
            Arrays.asList("technology"), 
            Arrays.asList());
        
        String conversationId = "throughput_conv";
        String userId = "throughput_user";
        
        int messageCount = 100;
        long startTime = System.nanoTime();
        
        for (int i = 0; i < messageCount; i++) {
            AdSuggestion suggestion = engine.processMessage(conversationId, userId, 
                "I need a smartphone " + i);
            assertNotNull("Suggestion should not be null", suggestion);
        }
        
        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        
        double throughput = (double) messageCount / (totalTimeMs / 1000.0); // messages per second
        
        System.out.println("Processed " + messageCount + " messages in " + totalTimeMs + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " messages/second");
        
        // Should handle high throughput
        assertTrue("Should process at least 10 messages per second", throughput > 10);
    }
    
    @Test
    public void testMemoryEfficiency() {
        ConversationalAdRelevanceEngine engine = new ConversationalAdRelevanceEngine();
        
        // Set up multiple users and conversations
        for (int i = 0; i < 50; i++) {
            String userId = "user_" + i;
            String conversationId = "conv_" + i;
            
            engine.updateUserPreferences(userId, 
                Arrays.asList("technology", "fashion"), 
                Arrays.asList());
            
            // Process some messages
            for (int j =  i; j < 5; j++) {
                engine.processMessage(conversationId, userId, 
                    "Message " + j + " from user " + i);
            }
        }
        
        // Get stats to verify memory usage is reasonable
        EngineStats stats = engine.getStats();
        
        System.out.println("Active conversations: " + stats.getActiveConversations());
        System.out.println("Total users: " + stats.getTotalUsers());
        System.out.println("Ad inventory size: " + stats.getAdInventorySize());
        
        // Should handle multiple users and conversations efficiently
        assertTrue("Should handle multiple conversations", stats.getActiveConversations() > 0);
        assertTrue("Should handle multiple users", stats.getTotalUsers() > 0);
        
        // Cleanup
        engine.shutdown();
    }
}
