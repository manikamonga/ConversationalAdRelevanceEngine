package com.adrelevance.llm;

import com.adrelevance.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for integrating with OpenAI ChatGPT for intelligent ad suggestions
 */
@Service
public class ChatGPTService {
    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;
    
    @Value("${openai.model:gpt-3.5-turbo}")
    private String openaiModel;
    
    public ChatGPTService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Analyzes conversation context using ChatGPT and generates intelligent ad suggestions
     */
    public CompletableFuture<ChatGPTAdSuggestion> analyzeContextAndSuggestAd(
            String conversationId, 
            String userId, 
            String message, 
            List<Message> conversationHistory,
            UserState userState) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build conversation context for ChatGPT
                String conversationContext = buildConversationContext(conversationHistory, userState);
                
                // Create prompt for ChatGPT
                String prompt = createAdSuggestionPrompt(message, conversationContext);
                
                // Call ChatGPT API
                String chatGPTResponse = callChatGPTAPI(prompt);
                
                // Parse ChatGPT response and extract ad suggestion
                return parseChatGPTResponse(chatGPTResponse, message);
                
            } catch (Exception e) {
                logger.error("Error analyzing context with ChatGPT for conversation {}: {}", conversationId, e.getMessage(), e);
                // Don't fall back to mock - let the error propagate
                throw new RuntimeException("ChatGPT API unavailable: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Creates a comprehensive conversation context for ChatGPT
     */
    private String buildConversationContext(List<Message> conversationHistory, UserState userState) {
        StringBuilder context = new StringBuilder();
        
        // Add user state information
        if (userState != null) {
            context.append("User Profile:\n");
            context.append("- Interests: ").append(String.join(", ", userState.getInterests())).append("\n");
            context.append("- Current Mood: ").append(userState.getCurrentMood()).append("\n");
            context.append("- Blocked Categories: ").append(String.join(", ", userState.getBlockedCategories())).append("\n");
            context.append("\n");
        }
        
        // Add conversation history
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            context.append("Recent Conversation:\n");
            for (Message msg : conversationHistory) {
                String role = msg.getType() == MessageType.USER_MESSAGE ? "User" : "Assistant";
                context.append(role).append(": ").append(msg.getContent()).append("\n");
            }
            context.append("\n");
        }
        
        return context.toString();
    }
    
    /**
     * Creates a prompt for ChatGPT to generate conversational responses with ad suggestions
     */
    private String createAdSuggestionPrompt(String currentMessage, String conversationContext) {
        return String.format("""
            You are a friendly, conversational AI assistant that can help users with various topics while occasionally suggesting relevant, contextual ads when appropriate.
            
            %s
            
            Current User Message: "%s"
            
            Your task is to:
            1. Respond naturally to the user's message as a conversational AI
            2. If the user mentions shopping, travel, technology, fashion, food, or any product/service need, include an ad_suggestion
            3. Keep the conversation engaging and human-like
            4. Always be helpful and friendly
            
            Please respond in the following JSON format (ensure valid JSON with no trailing commas):
            {
                "intent": "shopping|travel|technology|fashion|health|food|entertainment|conversation|other",
                "mood": "excited|happy|curious|neutral|frustrated|other",
                "confidence": 0.0-1.0,
                "conversational_response": "Your natural response to the user's message"
            }
            
            If you want to suggest an ad, add this field to the JSON:
            "ad_suggestion": {
                "title": "Ad Title",
                "description": "Brief ad description", 
                "category": "fashion|tech|travel|food|fitness|beauty|other",
                "call_to_action": "Shop Now|Learn More|Book Now|Get Started|etc",
                "url": "https://example.com/product",
                "relevance_reasoning": "Brief explanation of why this ad is relevant"
            }
            
            Guidelines:
            - Always provide a conversational_response
            - Be friendly, helpful, and engaging in your responses
            - If user mentions shopping, travel, technology, fashion, food, or any product need, include ad_suggestion
            - Make ad suggestions feel natural and helpful, not pushy
            - Keep responses conversational and natural
            - Consider user interests and mood when making suggestions
            """, conversationContext, currentMessage);
    }
    
    /**
     * Calls the ChatGPT API with the given prompt
     */
    private String callChatGPTAPI(String prompt) throws IOException {
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            logger.warn("OpenAI API key not configured, using mock response");
            return createMockChatGPTResponse(prompt);
        }
        
        logger.info("Attempting ChatGPT API call");
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", openaiModel);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", prompt);
        
        Request request = new Request.Builder()
                .url(openaiApiUrl)
                .addHeader("Authorization", "Bearer " + openaiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.get("application/json")
                ))
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                logger.error("ChatGPT API call failed: {} {} - Response: {}", 
                    response.code(), response.message(), responseBody);
                throw new IOException("ChatGPT API call failed: " + response.code() + " " + response.message() + " - " + responseBody);
            }
            
            logger.debug("ChatGPT API response: {}", responseBody);
            return responseBody;
        }
    }
    
    /**
     * Creates an intelligent mock response that mimics ChatGPT's behavior
     * This is a smarter fallback that analyzes context like ChatGPT would
     */
    private String createMockChatGPTResponse(String prompt) {
        // Extract the user message from the prompt
        String userMessage = extractUserMessageFromPrompt(prompt);
        String lowerMessage = userMessage.toLowerCase();
        
        // Analyze intent and context like ChatGPT would
        String intent = analyzeIntent(lowerMessage);
        String mood = analyzeMood(lowerMessage);
        double confidence = calculateConfidence(lowerMessage, intent);
        
        if (confidence < 0.3) {
            return createNoAdResponse();
        }
        
        // Generate contextual ad based on intent analysis
        return generateContextualAdResponse(intent, mood, confidence, userMessage);
    }
    
    /**
     * Extracts the user message from the ChatGPT prompt
     */
    private String extractUserMessageFromPrompt(String prompt) {
        // Look for the user message in the prompt
        String[] lines = prompt.split("\n");
        for (String line : lines) {
            if (line.contains("Current User Message:")) {
                return line.replace("Current User Message:", "").replace("\"", "").trim();
            }
        }
        return prompt; // Fallback to full prompt
    }
    
    /**
     * Analyzes user intent like ChatGPT would
     */
    private String analyzeIntent(String message) {
        // Shopping-related intents
        if (message.contains("buy") || message.contains("purchase") || message.contains("shop") || 
            message.contains("need") || message.contains("want") || message.contains("looking for") ||
            message.contains("what about") || message.contains("recommendation") || message.contains("rec")) {
            
            if (message.contains("dress") || message.contains("fashion") || message.contains("style") || 
                message.contains("clothes") || message.contains("outfit")) {
                return "shopping";
            }
            if (message.contains("phone") || message.contains("smartphone") || message.contains("tech") || 
                message.contains("gadget") || message.contains("device")) {
                return "technology";
            }
            if (message.contains("hotel") || message.contains("accommodation") || message.contains("stay") || 
                message.contains("vacation") || message.contains("travel") || message.contains("trip") ||
                message.contains("milan") || message.contains("italy") || message.contains("booking")) {
                return "travel";
            }
            if (message.contains("restaurant") || message.contains("food") || message.contains("dining") || 
                message.contains("italian") || message.contains("pizza") || message.contains("pasta") ||
                message.contains("meal") || message.contains("eat")) {
                return "food";
            }
            if (message.contains("fitness") || message.contains("workout") || message.contains("gym") || 
                message.contains("exercise") || message.contains("training")) {
                return "health";
            }
            if (message.contains("beauty") || message.contains("skincare") || message.contains("makeup") || 
                message.contains("cosmetics")) {
                return "shopping";
            }
            if (message.contains("bag") || message.contains("purse") || message.contains("handbag") || 
                message.contains("jewelry") || message.contains("watch") || message.contains("accessories")) {
                return "shopping";
            }
        }
        
        return "other";
    }
    
    /**
     * Analyzes user mood like ChatGPT would
     */
    private String analyzeMood(String message) {
        if (message.contains("excited") || message.contains("amazing") || message.contains("perfect") || 
            message.contains("love") || message.contains("great")) {
            return "excited";
        }
        if (message.contains("need") || message.contains("want") || message.contains("looking")) {
            return "curious";
        }
        return "neutral";
    }
    
    /**
     * Calculates confidence score like ChatGPT would
     */
    private double calculateConfidence(String message, String intent) {
        if (intent.equals("other")) {
            return 0.0;
        }
        
        // Higher confidence for specific product mentions
        if (message.contains("need") || message.contains("want") || message.contains("buy")) {
            return 0.8;
        }
        
        // Medium confidence for general interest
        if (message.contains("looking") || message.contains("recommendation")) {
            return 0.6;
        }
        
        return 0.4;
    }
    
    /**
     * Generates contextual ad response like ChatGPT would
     */
    private String generateContextualAdResponse(String intent, String mood, double confidence, String originalMessage) {
        switch (intent) {
            case "shopping":
                if (originalMessage.toLowerCase().contains("dress") || originalMessage.toLowerCase().contains("fashion")) {
                    return createFashionAdResponse(mood, confidence);
                } else if (originalMessage.toLowerCase().contains("beauty") || originalMessage.toLowerCase().contains("skincare")) {
                    return createBeautyAdResponse(mood, confidence);
                } else if (originalMessage.toLowerCase().contains("bag") || originalMessage.toLowerCase().contains("accessories")) {
                    return createAccessoriesAdResponse(mood, confidence);
                }
                break;
            case "technology":
                return createTechnologyAdResponse(mood, confidence);
            case "travel":
                return createTravelAdResponse(mood, confidence);
            case "food":
                return createFoodAdResponse(mood, confidence);
            case "health":
                return createFitnessAdResponse(mood, confidence);
        }
        
        return createNoAdResponse();
    }
    
    private String createFashionAdResponse(String mood, double confidence) {
        return String.format("""
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"shopping\\", \\"mood\\": \\"%s\\", \\"confidence\\": %s, \\"ad_suggestion\\": {\\"title\\": \\"Summer Collection\\", \\"description\\": \\"Discover the latest summer fashion trends\\", \\"category\\": \\"fashion\\", \\"conversational_response\\": \\"Hey! I noticed you're into style. Our new summer collection is absolutely stunning! 🌸\\", \\"call_to_action\\": \\"Shop Now\\", \\"url\\": \\"https://fashionbrand.com/summer-collection\\", \\"relevance_reasoning\\": \\"User is asking about fashion and clothing items\\"}}"
                    }
                }]
            }
            """, mood, confidence);
    }
    
    private String createTechnologyAdResponse(String mood, double confidence) {
        return String.format("""
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"technology\\", \\"mood\\": \\"%s\\", \\"confidence\\": %s, \\"ad_suggestion\\": {\\"title\\": \\"Latest Smartphone\\", \\"description\\": \\"Experience cutting-edge technology\\", \\"category\\": \\"tech\\", \\"conversational_response\\": \\"Speaking of tech, have you seen the latest smartphone? It's pretty amazing! 📱\\", \\"call_to_action\\": \\"Learn More\\", \\"url\\": \\"https://techcorp.com/latest-smartphone\\", \\"relevance_reasoning\\": \\"User is asking about smartphones and technology\\"}}"
                    }
                }]
            }
            """, mood, confidence);
    }
    
    private String createTravelAdResponse(String mood, double confidence) {
        return String.format("""
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"travel\\", \\"mood\\": \\"%s\\", \\"confidence\\": %s, \\"ad_suggestion\\": {\\"title\\": \\"Luxury Hotel Collection\\", \\"description\\": \\"Discover amazing hotels and accommodations worldwide\\", \\"category\\": \\"travel\\", \\"conversational_response\\": \\"Looking for amazing accommodations? I know the perfect hotels that will make your stay unforgettable! 🏨\\", \\"call_to_action\\": \\"Book Hotel\\", \\"url\\": \\"https://luxuryhotels.com/collection\\", \\"relevance_reasoning\\": \\"User is asking about hotels and travel accommodations\\"}}"
                    }
                }]
            }
            """, mood, confidence);
    }
    
    private String createFoodAdResponse(String mood, double confidence) {
        return String.format("""
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"food\\", \\"mood\\": \\"%s\\", \\"confidence\\": %s, \\"ad_suggestion\\": {\\"title\\": \\"Authentic Italian Dining\\", \\"description\\": \\"Experience the finest Italian cuisine in town\\", \\"category\\": \\"food\\", \\"conversational_response\\": \\"Craving amazing food? I know the perfect Italian restaurant that will blow your mind! 🍝\\", \\"call_to_action\\": \\"Book Table\\", \\"url\\": \\"https://italianrestaurant.com/book-table\\", \\"relevance_reasoning\\": \\"User is asking about restaurants and food\\"}}"
                    }
                }]
            }
            """, mood, confidence);
    }
    
    private String createFitnessAdResponse(String mood, double confidence) {
        return String.format("""
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"health\\", \\"mood\\": \\"%s\\", \\"confidence\\": %s, \\"ad_suggestion\\": {\\"title\\": \\"Premium Fitness Program\\", \\"description\\": \\"Transform your fitness journey with expert guidance\\", \\"category\\": \\"fitness\\", \\"conversational_response\\": \\"Ready to crush your fitness goals? This program is a game-changer! 💪\\", \\"call_to_action\\": \\"Start Today\\", \\"url\\": \\"https://fitlife.com/get-fit-fast\\", \\"relevance_reasoning\\": \\"User is asking about fitness and exercise\\"}}"
                    }
                }]
            }
            """, mood, confidence);
    }
    
    private String createBeautyAdResponse(String mood, double confidence) {
        return String.format("""
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"shopping\\", \\"mood\\": \\"%s\\", \\"confidence\\": %s, \\"ad_suggestion\\": {\\"title\\": \\"Natural Beauty Collection\\", \\"description\\": \\"Discover your natural glow with premium skincare\\", \\"category\\": \\"beauty\\", \\"conversational_response\\": \\"Want that natural glow? This skincare line is absolutely magical! ✨\\", \\"call_to_action\\": \\"Shop Collection\\", \\"url\\": \\"https://beautybrand.com/natural-skincare\\", \\"relevance_reasoning\\": \\"User is asking about beauty and skincare\\"}}"
                    }
                }]
            }
            """, mood, confidence);
    }
    
    private String createAccessoriesAdResponse(String mood, double confidence) {
        return String.format("""
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"shopping\\", \\"mood\\": \\"%s\\", \\"confidence\\": %s, \\"ad_suggestion\\": {\\"title\\": \\"Luxury Accessories Collection\\", \\"description\\": \\"Elevate your style with premium bags and accessories\\", \\"category\\": \\"accessories\\", \\"conversational_response\\": \\"Looking to elevate your style? Our luxury accessories collection is absolutely stunning! 👜\\", \\"call_to_action\\": \\"Shop Now\\", \\"url\\": \\"https://luxuryaccessories.com/collection\\", \\"relevance_reasoning\\": \\"User is asking about bags, accessories, or fashion items\\"}}"
                    }
                }]
            }
            """, mood, confidence);
    }
    
    private String createNoAdResponse() {
        return """
            {
                "choices": [{
                    "message": {
                        "content": "{\\"intent\\": \\"other\\", \\"mood\\": \\"neutral\\", \\"confidence\\": 0.0}"
                    }
                }]
            }
            """;

    }
    
    /**
     * Parses the ChatGPT API response and extracts conversational response with optional ad suggestion
     */
    private ChatGPTAdSuggestion parseChatGPTResponse(String chatGPTResponse, String originalMessage) {
        try {
            JsonNode responseNode = objectMapper.readTree(chatGPTResponse);
            String content = responseNode.path("choices").path(0).path("message").path("content").asText();
            
            // Parse the JSON content from ChatGPT's response
            JsonNode suggestionNode = objectMapper.readTree(content);
            
            String intent = suggestionNode.path("intent").asText();
            String mood = suggestionNode.path("mood").asText();
            double confidence = suggestionNode.path("confidence").asDouble();
            String conversationalResponse = suggestionNode.path("conversational_response").asText();
            
            // Extract ad suggestion if present
            JsonNode adNode = suggestionNode.path("ad_suggestion");
            Ad ad = null;
            
            if (!adNode.isMissingNode() && confidence >= 0.3) {
                ad = new Ad(
                    "chatgpt_" + System.currentTimeMillis(),
                    adNode.path("title").asText(),
                    adNode.path("description").asText(),
                    "ChatGPT"
                );
                ad.addCategory(adNode.path("category").asText());
                ad.setCallToAction(adNode.path("call_to_action").asText());
                
                // Create conversational template with clickable link
                String url = adNode.path("url").asText();
                String callToAction = adNode.path("call_to_action").asText();
                
                String template = String.format("%s <a href='%s' target='_blank'>%s</a>", 
                    conversationalResponse, url, callToAction);
                ad.setConversationalTemplate(template);
            } else {
                // No ad, but still provide conversational response
                ad = new Ad(
                    "chatgpt_" + System.currentTimeMillis(),
                    "Conversational Response",
                    "AI Assistant Response",
                    "ChatGPT"
                );
                ad.addCategory("conversation");
                ad.setCallToAction("");
                ad.setConversationalTemplate(conversationalResponse);
            }
            
            return new ChatGPTAdSuggestion(originalMessage, ad, confidence, conversationalResponse);
            
        } catch (Exception e) {
            logger.error("Error parsing ChatGPT response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse ChatGPT response: " + e.getMessage(), e);
        }
    }
    

    
    /**
     * Data class for ChatGPT ad suggestions
     */
    public static class ChatGPTAdSuggestion {
        private final String originalMessage;
        private final Ad ad;
        private final double confidence;
        private final String reasoning;
        
        public ChatGPTAdSuggestion(String originalMessage, Ad ad, double confidence, String reasoning) {
            this.originalMessage = originalMessage;
            this.ad = ad;
            this.confidence = confidence;
            this.reasoning = reasoning;
        }
        
        public String getOriginalMessage() { return originalMessage; }
        public Ad getAd() { return ad; }
        public double getConfidence() { return confidence; }
        public String getReasoning() { return reasoning; }
        public boolean hasAd() { return ad != null && confidence >= 0.3; }
    }
}
