package com.adrelevance.engine;

import com.adrelevance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Generates conversational ad responses based on context and mood
 */
public class ConversationalResponseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ConversationalResponseGenerator.class);
    
    private final Map<ConversationMood, List<String>> moodTemplates;
    private final Map<String, List<String>> intentTemplates;
    private final Random random;

    public ConversationalResponseGenerator() {
        this.moodTemplates = initializeMoodTemplates();
        this.intentTemplates = initializeIntentTemplates();
        this.random = new Random();
    }

    /**
     * Generates a conversational ad response based on the ad and context
     */
    public String generateResponse(Ad ad, ConversationContext context) {
        if (ad == null || context == null) {
            return "Check out this amazing offer!";
        }

        // Use ad's conversational template if available
        if (ad.getConversationalTemplate() != null && !ad.getConversationalTemplate().trim().isEmpty()) {
            return personalizeTemplate(ad.getConversationalTemplate(), context);
        }

        // Generate based on mood and intent
        String response = generateMoodBasedResponse(ad, context);
        if (response != null) {
            return response;
        }

        // Fallback to intent-based response
        response = generateIntentBasedResponse(ad, context);
        if (response != null) {
            return response;
        }

        // Default response
        return generateDefaultResponse(ad, context);
    }

    private String personalizeTemplate(String template, ConversationContext context) {
        String personalized = template;
        
        // Replace placeholders with context-specific content
        if (context.getUserState() != null && context.getUserState().getCurrentMood() != null) {
            String moodEmoji = getMoodEmoji(context.getUserState().getCurrentMood());
            personalized = personalized.replace("{mood_emoji}", moodEmoji);
        }

        // Add call to action if not present
        if (!personalized.contains("!") && !personalized.contains("?")) {
            personalized += "!";
        }

        return personalized;
    }

    private String generateMoodBasedResponse(Ad ad, ConversationContext context) {
        if (context.getMood() == null) {
            return null;
        }

        List<String> templates = moodTemplates.get(context.getMood());
        if (templates == null || templates.isEmpty()) {
            return null;
        }

        String template = templates.get(random.nextInt(templates.size()));
        return formatResponse(template, ad, context);
    }

    private String generateIntentBasedResponse(Ad ad, ConversationContext context) {
        if (context.getDetectedIntents() == null || context.getDetectedIntents().isEmpty()) {
            return null;
        }

        for (String intent : context.getDetectedIntents()) {
            List<String> templates = intentTemplates.get(intent);
            if (templates != null && !templates.isEmpty()) {
                String template = templates.get(random.nextInt(templates.size()));
                return formatResponse(template, ad, context);
            }
        }

        return null;
    }

    private String generateDefaultResponse(Ad ad, ConversationContext context) {
        String[] defaultTemplates = {
            "Hey! I think you might love {brand} - {title}!",
            "Speaking of {category}, have you checked out {brand}?",
            "I came across {title} and thought of you!",
            "You know what's awesome? {title} from {brand}!",
            "Just discovered {brand} and it's pretty amazing!"
        };

        String template = defaultTemplates[random.nextInt(defaultTemplates.length)];
        return formatResponse(template, ad, context);
    }

    private String formatResponse(String template, Ad ad, ConversationContext context) {
        String response = template;
        
        // Replace placeholders
        response = response.replace("{brand}", ad.getBrandName());
        response = response.replace("{title}", ad.getTitle());
        response = response.replace("{description}", ad.getDescription());
        
        if (ad.getCategories() != null && !ad.getCategories().isEmpty()) {
            response = response.replace("{category}", ad.getCategories().get(0));
        }
        
        // Add mood-appropriate emoji
        if (context.getMood() != null) {
            String moodEmoji = getMoodEmoji(context.getMood());
            if (!response.contains("!")) {
                response += " " + moodEmoji;
            }
        }

        return response;
    }

    private String getMoodEmoji(ConversationMood mood) {
        switch (mood) {
            case POSITIVE: return "😊";
            case EXCITED: return "🎉";
            case HUMOROUS: return "😄";
            case CURIOUS: return "🤔";
            case FRUSTRATED: return "😤";
            case NEGATIVE: return "😔";
            default: return "✨";
        }
    }

    private String getMoodEmoji(UserMood mood) {
        switch (mood) {
            case HAPPY: return "😊";
            case EXCITED: return "🎉";
            case CURIOUS: return "🤔";
            case FRUSTRATED: return "😤";
            case SAD: return "😔";
            case ANGRY: return "😠";
            default: return "✨";
        }
    }

    private Map<ConversationMood, List<String>> initializeMoodTemplates() {
        Map<ConversationMood, List<String>> templates = new HashMap<>();
        
        // Positive mood templates
        templates.put(ConversationMood.POSITIVE, Arrays.asList(
            "You seem to be in a great mood! Perfect time to check out {brand}! 😊",
            "Your positive energy is contagious! You'll love {title}! ✨",
            "Love your vibe! {brand} has something amazing for you! 🌟"
        ));

        // Excited mood templates
        templates.put(ConversationMood.EXCITED, Arrays.asList(
            "Your excitement is infectious! Wait till you see {title}! 🎉",
            "I can feel your energy! {brand} is going to blow your mind! 🚀",
            "You're pumped up! Perfect timing for {title}! 💪"
        ));

        // Curious mood templates
        templates.put(ConversationMood.CURIOUS, Arrays.asList(
            "I sense your curiosity! Let me tell you about {title}... 🤔",
            "Your inquisitive mind will love discovering {brand}! 🔍",
            "Since you're curious, you should definitely check out {title}! 💡"
        ));

        // Humorous mood templates
        templates.put(ConversationMood.HUMOROUS, Arrays.asList(
            "You're hilarious! {brand} has a sense of humor too! 😄",
            "Love your jokes! {title} is no joke though - it's amazing! 😂",
            "Your wit is sharp! {brand} is pretty sharp too! 😎"
        ));

        // Frustrated mood templates
        templates.put(ConversationMood.FRUSTRATED, Arrays.asList(
            "I hear you're frustrated. Maybe {title} can help turn things around? 💪",
            "When things get tough, {brand} has your back! 💪",
            "Don't let frustration get you down! {title} might be the solution! ✨"
        ));

        return templates;
    }

    private Map<String, List<String>> initializeIntentTemplates() {
        Map<String, List<String>> templates = new HashMap<>();
        
        // Shopping intent templates
        templates.put("shopping", Arrays.asList(
            "Since you're in shopping mode, you have to see {title}! 🛍️",
            "Shopping spree? Don't forget to check out {brand}! 💳",
            "Your shopping list needs {title}! 📝"
        ));

        // Research intent templates
        templates.put("research", Arrays.asList(
            "Doing some research? {brand} has all the details you need! 🔍",
            "Research mode activated! {title} is worth investigating! 📊",
            "Since you're researching, {brand} should be on your list! 📋"
        ));

        // Entertainment intent templates
        templates.put("entertainment", Arrays.asList(
            "Looking for entertainment? {title} is pure fun! 🎮",
            "Entertainment time! {brand} knows how to keep you entertained! 🎬",
            "Fun seeker alert! {title} is your next entertainment fix! 🎉"
        ));

        // Travel intent templates
        templates.put("travel", Arrays.asList(
            "Planning a trip? {brand} has amazing travel deals! ✈️",
            "Wanderlust calling? {title} is your travel companion! 🌍",
            "Adventure awaits with {brand}! 🗺️"
        ));

        // Food intent templates
        templates.put("food", Arrays.asList(
            "Foodie alert! {title} is a culinary delight! 🍽️",
            "Hungry for something new? {brand} has you covered! 🍕",
            "Your taste buds will thank you for {title}! 👨‍🍳"
        ));

        return templates;
    }

    /**
     * Generates a follow-up response after user interaction
     */
    public String generateFollowUpResponse(Ad ad, ConversationContext context, String userResponse) {
        if (userResponse == null) {
            return "What do you think about " + ad.getTitle() + "?";
        }

        String lowerResponse = userResponse.toLowerCase();
        
        if (lowerResponse.contains("yes") || lowerResponse.contains("love") || lowerResponse.contains("great")) {
            return "Awesome! I knew you'd love " + ad.getBrandName() + "! " + ad.getCallToAction() + " 🎉";
        } else if (lowerResponse.contains("no") || lowerResponse.contains("not") || lowerResponse.contains("don't")) {
            return "No worries! Maybe next time. What else are you interested in? 🤔";
        } else if (lowerResponse.contains("maybe") || lowerResponse.contains("think")) {
            return "Take your time! " + ad.getTitle() + " will be here when you're ready! 😊";
        } else {
            return "Interesting! Tell me more about what you're looking for! 💬";
        }
    }
}
