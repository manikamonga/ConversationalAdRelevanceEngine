package com.adrelevance.engine;

import com.adrelevance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Matches ads based on conversation context and user state
 */
public class AdMatchingEngine {
    private static final Logger logger = LoggerFactory.getLogger(AdMatchingEngine.class);
    
    private final List<Ad> adInventory;
    private final double topicWeight = 0.4;
    private final double moodWeight = 0.3;
    private final double intentWeight = 0.2;
    private final double userPreferenceWeight = 0.1;

    public AdMatchingEngine() {
        this.adInventory = new ArrayList<>();
        initializeAdInventory();
    }

    /**
     * Finds the most relevant ads for the given conversation context
     */
    public List<Ad> findRelevantAds(ConversationContext context, int maxResults) {
        if (context == null) {
            return new ArrayList<>();
        }

        List<Ad> relevantAds = adInventory.stream()
                .filter(Ad::isActive)
                .map(ad -> {
                    double score = calculateRelevanceScore(ad, context);
                    ad.setRelevanceScore(score);
                    return ad;
                })
                .filter(ad -> ad.getRelevanceScore() > 0.1) // Minimum relevance threshold (temporarily lowered)
                .sorted((a1, a2) -> Double.compare(a2.getRelevanceScore(), a1.getRelevanceScore()))
                .limit(maxResults)
                .collect(Collectors.toList());

        logger.info("Found {} relevant ads for conversation {}", relevantAds.size(), context.getConversationId());
        return relevantAds;
    }

    /**
     * Calculates relevance score for an ad based on conversation context
     */
    private double calculateRelevanceScore(Ad ad, ConversationContext context) {
        double topicScore = calculateTopicRelevance(ad, context);
        double moodScore = calculateMoodRelevance(ad, context);
        double intentScore = calculateIntentRelevance(ad, context);
        double userPreferenceScore = calculateUserPreferenceRelevance(ad, context);

        double totalScore = (topicScore * topicWeight) +
                          (moodScore * moodWeight) +
                          (intentScore * intentWeight) +
                          (userPreferenceScore * userPreferenceWeight);

        logger.debug("Ad '{}' relevance scores - Topic: {}, Mood: {}, Intent: {}, UserPref: {}, Total: {}", 
                   ad.getId(), topicScore, moodScore, intentScore, userPreferenceScore, totalScore);

        return Math.min(1.0, totalScore);
    }

    private double calculateTopicRelevance(Ad ad, ConversationContext context) {
        if (context.getTopicWeights() == null || context.getTopicWeights().isEmpty()) {
            return 0.0;
        }

        double maxScore = 0.0;
        for (Map.Entry<String, Double> topicEntry : context.getTopicWeights().entrySet()) {
            String topic = topicEntry.getKey();
            Double topicWeight = topicEntry.getValue();
            
            Double adTopicRelevance = ad.getTopicRelevance().get(topic);
            if (adTopicRelevance != null) {
                double score = topicWeight * adTopicRelevance;
                maxScore = Math.max(maxScore, score);
            }
        }

        return maxScore;
    }

    private double calculateMoodRelevance(Ad ad, ConversationContext context) {
        if (context.getMood() == null || context.getUserState() == null) {
            return 0.0;
        }

        UserMood userMood = context.getUserState().getCurrentMood();
        Double moodRelevance = ad.getMoodRelevance().get(userMood);
        
        return moodRelevance != null ? moodRelevance : 0.0;
    }

    private double calculateIntentRelevance(Ad ad, ConversationContext context) {
        if (context.getDetectedIntents() == null || context.getDetectedIntents().isEmpty()) {
            return 0.0;
        }

        double maxScore = 0.0;
        for (String intent : context.getDetectedIntents()) {
            // Check if ad categories or keywords match the intent
            if (ad.getCategories().contains(intent) || 
                ad.getKeywords().contains(intent) ||
                ad.getTargetAudience().contains(intent)) {
                maxScore = Math.max(maxScore, 0.8);
                logger.debug("Intent '{}' matched ad '{}' with score 0.8", intent, ad.getId());
            } else {
                logger.debug("Intent '{}' did not match ad '{}' (categories: {}, keywords: {})", 
                           intent, ad.getId(), ad.getCategories(), ad.getKeywords());
            }
        }

        return maxScore;
    }

    private double calculateUserPreferenceRelevance(Ad ad, ConversationContext context) {
        if (context.getUserState() == null) {
            return 0.0;
        }

        UserState userState = context.getUserState();
        double score = 0.0;

        // Check if user has shown interest in ad categories
        for (String category : ad.getCategories()) {
            if (userState.getInterests().contains(category)) {
                score += 0.3;
            }
        }

        // Check if user has interacted with similar ads before
        Integer interactionCount = userState.getAdInteractionHistory().get(ad.getId());
        if (interactionCount != null && interactionCount > 0) {
            score += Math.min(0.4, interactionCount * 0.1);
        }

        return Math.min(1.0, score);
    }

    /**
     * Initializes the ad inventory with sample ads
     */
    private void initializeAdInventory() {
        // Fashion ads
        Ad fashionAd = new Ad("fashion_001", "Summer Collection", "Discover the latest summer fashion trends", "FashionBrand");
        fashionAd.setCallToAction("Shop Now");
        fashionAd.addCategory("fashion");
        fashionAd.addKeyword("style");
        fashionAd.addKeyword("trendy");
        fashionAd.addKeyword("fashion");
        fashionAd.addKeyword("clothes");
        fashionAd.addKeyword("outfit");
        fashionAd.addKeyword("dress");
        fashionAd.addKeyword("shoes");
        fashionAd.addKeyword("bag");
        fashionAd.addKeyword("accessories");
        fashionAd.setTopicRelevance("fashion", 0.9);
        fashionAd.setMoodRelevance(UserMood.HAPPY, 0.8);
        fashionAd.setMoodRelevance(UserMood.EXCITED, 0.7);
        fashionAd.setConversationalTemplate("Hey! I noticed you're into style. Our new summer collection is absolutely stunning! 🌸 <a href='https://fashionbrand.com/summer-collection' target='_blank'>Shop Now</a>");
        fashionAd.setType(AdType.PRODUCT_PROMOTION);
        adInventory.add(fashionAd);

        // Technology ads
        Ad techAd = new Ad("tech_001", "Latest Smartphone", "Experience cutting-edge technology", "TechCorp");
        techAd.setCallToAction("Learn More");
        techAd.addCategory("electronics");
        techAd.addCategory("technology");
        techAd.addKeyword("smartphone");
        techAd.addKeyword("phone");
        techAd.addKeyword("mobile");
        techAd.addKeyword("tech");
        techAd.addKeyword("technology");
        techAd.addKeyword("innovation");
        techAd.addKeyword("device");
        techAd.setTopicRelevance("electronics", 0.95);
        techAd.setTopicRelevance("technology", 0.9);
        techAd.setMoodRelevance(UserMood.CURIOUS, 0.8);
        techAd.setMoodRelevance(UserMood.EXCITED, 0.9);
        techAd.setConversationalTemplate("Speaking of tech, have you seen the latest smartphone? It's pretty amazing! 📱 <a href='https://techcorp.com/latest-smartphone' target='_blank'>Learn More</a>");
        techAd.setType(AdType.PRODUCT_PROMOTION);
        adInventory.add(techAd);

        // Travel ads
        Ad travelAd = new Ad("travel_001", "Dream Vacation", "Plan your perfect getaway", "TravelAgency");
        travelAd.setCallToAction("Book Now");
        travelAd.addCategory("travel");
        travelAd.addKeyword("vacation");
        travelAd.addKeyword("trip");
        travelAd.addKeyword("travel");
        travelAd.addKeyword("destination");
        travelAd.addKeyword("getaway");
        travelAd.addKeyword("holiday");
        travelAd.setTopicRelevance("travel", 0.9);
        travelAd.setMoodRelevance(UserMood.EXCITED, 0.9);
        travelAd.setMoodRelevance(UserMood.HAPPY, 0.7);
        travelAd.setConversationalTemplate("Dreaming of a vacation? I know the perfect place for your next adventure! ✈️ <a href='https://travelagency.com/dream-vacation' target='_blank'>Book Now</a>");
        travelAd.setType(AdType.SPECIAL_OFFER);
        adInventory.add(travelAd);

        // Food ads
        Ad foodAd = new Ad("food_001", "Delicious Recipes", "Cook like a chef at home", "FoodNetwork");
        foodAd.setCallToAction("Get Recipes");
        foodAd.addCategory("food");
        foodAd.addKeyword("cooking");
        foodAd.addKeyword("recipes");
        foodAd.setTopicRelevance("food", 0.9);
        foodAd.setMoodRelevance(UserMood.HAPPY, 0.6);
        foodAd.setMoodRelevance(UserMood.CURIOUS, 0.7);
        foodAd.setConversationalTemplate("Love cooking? I've got some amazing recipes that'll make you look like a pro chef! 👨‍🍳 <a href='https://foodnetwork.com/delicious-recipes' target='_blank'>Get Recipes</a>");
        foodAd.setType(AdType.EDUCATIONAL);
        adInventory.add(foodAd);

        // Fitness ads
        Ad fitnessAd = new Ad("fitness_001", "Get Fit Fast", "Transform your body in 30 days", "FitLife");
        fitnessAd.setCallToAction("Start Today");
        fitnessAd.addCategory("health");
        fitnessAd.addCategory("sports");
        fitnessAd.addKeyword("fitness");
        fitnessAd.addKeyword("workout");
        fitnessAd.setTopicRelevance("sports", 0.9);
        fitnessAd.setTopicRelevance("health", 0.8);
        fitnessAd.setMoodRelevance(UserMood.EXCITED, 0.8);
        fitnessAd.setMoodRelevance(UserMood.CURIOUS, 0.6);
        fitnessAd.setConversationalTemplate("Ready to crush your fitness goals? This program is a game-changer! 💪 <a href='https://fitlife.com/get-fit-fast' target='_blank'>Start Today</a>");
        fitnessAd.setType(AdType.PRODUCT_PROMOTION);
        adInventory.add(fitnessAd);

        // Beauty ads
        Ad beautyAd = new Ad("beauty_001", "Natural Skincare", "Glow from within", "BeautyBrand");
        beautyAd.setCallToAction("Shop Collection");
        beautyAd.addCategory("beauty");
        beautyAd.addKeyword("skincare");
        beautyAd.addKeyword("natural");
        beautyAd.setTopicRelevance("beauty", 0.9);
        beautyAd.setMoodRelevance(UserMood.HAPPY, 0.7);
        beautyAd.setMoodRelevance(UserMood.CALM, 0.8);
        beautyAd.setConversationalTemplate("Want that natural glow? This skincare line is absolutely magical! ✨ <a href='https://beautybrand.com/natural-skincare' target='_blank'>Shop Collection</a>");
        beautyAd.setType(AdType.BRAND_AWARENESS);
        adInventory.add(beautyAd);

        logger.info("Initialized ad inventory with {} ads", adInventory.size());
    }

    /**
     * Adds a new ad to the inventory
     */
    public void addAd(Ad ad) {
        adInventory.add(ad);
        logger.info("Added new ad to inventory: {}", ad.getId());
    }

    /**
     * Gets the current ad inventory size
     */
    public int getInventorySize() {
        return adInventory.size();
    }
}
