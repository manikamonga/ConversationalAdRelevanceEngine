import { Message, AdSuggestion, UserState, EngineStats, PerformanceMetrics } from '../types';

// Mock data for demonstration
const mockAds = [
  {
    id: 'ad_001',
    title: 'Latest Smartphone',
    description: 'Experience the future with our cutting-edge smartphone featuring AI-powered camera and lightning-fast performance.',
    brandName: 'TechCorp',
    callToAction: 'Shop Now',
    categories: ['technology', 'smartphones', 'electronics'],
    type: 'PRODUCT_PROMOTION',
    relevanceScore: 0.84
  },
  {
    id: 'ad_002',
    title: 'Summer Collection',
    description: 'Discover our vibrant summer collection with trendy styles and comfortable fabrics for the perfect warm-weather look.',
    brandName: 'FashionHub',
    callToAction: 'Explore Collection',
    categories: ['fashion', 'clothing', 'summer'],
    type: 'BRAND_AWARENESS',
    relevanceScore: 0.76
  },
  {
    id: 'ad_003',
    title: 'Dream Vacation',
    description: 'Escape to paradise with our exclusive travel packages to the world\'s most beautiful destinations.',
    brandName: 'TravelEase',
    callToAction: 'Book Now',
    categories: ['travel', 'vacation', 'tourism'],
    type: 'SPECIAL_OFFER',
    relevanceScore: 0.92
  },
  {
    id: 'ad_004',
    title: 'Fitness Revolution',
    description: 'Transform your life with our revolutionary fitness program designed for real results.',
    brandName: 'FitLife',
    callToAction: 'Start Free Trial',
    categories: ['fitness', 'health', 'wellness'],
    type: 'EDUCATIONAL',
    relevanceScore: 0.68
  },
  {
    id: 'ad_005',
    title: 'Gourmet Delights',
    description: 'Savor the finest culinary experiences with our curated selection of gourmet foods and premium ingredients.',
    brandName: 'CulinaryCraft',
    callToAction: 'Order Now',
    categories: ['food', 'gourmet', 'culinary'],
    type: 'RECOMMENDATION',
    relevanceScore: 0.73
  }
];

class ApiService {
  private baseUrl: string;
  private conversationId: string;
  private userId: string;

  constructor() {
    this.baseUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';
    this.conversationId = this.generateId();
    this.userId = this.generateId();
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }

  private async simulateLatency(): Promise<void> {
    // Simulate realistic latency between 10-50ms
    const delay = Math.random() * 40 + 10;
    await new Promise(resolve => setTimeout(resolve, delay));
  }

  async processMessage(message: string): Promise<AdSuggestion | null> {
    await this.simulateLatency();

    // Enhanced keyword-based ad matching for demo
    const messageLower = message.toLowerCase();
    let bestAd = null;
    let bestScore = 0;

    // Define keyword mappings for better matching
    const keywordMappings: Record<string, string[]> = {
      'smartphone': ['technology', 'smartphones', 'electronics', 'phone'],
      'phone': ['technology', 'smartphones', 'electronics', 'smartphone'],
      'tech': ['technology', 'electronics'],
      'technology': ['technology', 'electronics'],
      'clothes': ['fashion', 'clothing', 'style'],
      'fashion': ['fashion', 'clothing', 'style'],
      'summer': ['fashion', 'clothing', 'summer'],
      'vacation': ['travel', 'vacation', 'tourism'],
      'travel': ['travel', 'vacation', 'tourism'],
      'trip': ['travel', 'vacation', 'tourism'],
      'fitness': ['fitness', 'health', 'wellness'],
      'workout': ['fitness', 'health', 'wellness'],
      'food': ['food', 'gourmet', 'culinary'],
      'restaurant': ['food', 'gourmet', 'culinary'],
      'dining': ['food', 'gourmet', 'culinary']
    };

    for (const ad of mockAds) {
      let score = 0;
      
      // Check direct category matches
      for (const category of ad.categories) {
        if (messageLower.includes(category)) {
          score += 0.4;
        }
      }

      // Check keyword mappings
      for (const [keyword, relatedCategories] of Object.entries(keywordMappings)) {
        if (messageLower.includes(keyword)) {
          for (const category of ad.categories) {
            if (relatedCategories.includes(category)) {
              score += 0.35;
              break;
            }
          }
        }
      }

      // Check title/description matches
      if (messageLower.includes(ad.title.toLowerCase())) {
        score += 0.5;
      }
      if (messageLower.includes(ad.brandName.toLowerCase())) {
        score += 0.3;
      }

      // Check description keywords
      const descriptionLower = ad.description.toLowerCase();
      if (messageLower.includes('new') && descriptionLower.includes('latest')) {
        score += 0.2;
      }
      if (messageLower.includes('need') && descriptionLower.includes('perfect')) {
        score += 0.1;
      }

      // Add some randomness for demo
      score += Math.random() * 0.1;

      if (score > bestScore) {
        bestScore = score;
        bestAd = ad;
      }
    }

    // Lower the threshold to be more responsive
    if (bestAd && bestScore > 0.2) {
      const responses = [
        `Hey! I noticed you're interested in ${bestAd.categories[0]}. ${bestAd.title} might be perfect for you! 🚀`,
        `Speaking of ${bestAd.categories[0]}, have you seen ${bestAd.title}? It's pretty amazing! ✨`,
        `Since you mentioned ${bestAd.categories[0]}, I thought you might like ${bestAd.title}. Check it out! 💫`,
        `I couldn't help but think of ${bestAd.title} when you mentioned ${bestAd.categories[0]}. It's worth a look! 🌟`,
        `Perfect timing! ${bestAd.title} is exactly what you're looking for in ${bestAd.categories[0]}. 🎯`,
        `You know what would be great for you? ${bestAd.title}! It's one of the best in ${bestAd.categories[0]}. ⭐`
      ];

      return {
        ad: { ...bestAd, relevanceScore: bestScore },
        response: responses[Math.floor(Math.random() * responses.length)],
        relevanceScore: bestScore
      };
    }

    return null;
  }

  async updateUserPreferences(preferences: Partial<UserState>): Promise<void> {
    await this.simulateLatency();
    // In a real implementation, this would call the Java backend
    console.log('Updating user preferences:', preferences);
  }

  async getStats(): Promise<EngineStats> {
    await this.simulateLatency();
    
    return {
      activeConversations: Math.floor(Math.random() * 50) + 10,
      adInventorySize: mockAds.length,
      totalUsers: Math.floor(Math.random() * 1000) + 100,
      averageLatency: Math.floor(Math.random() * 30) + 15,
      throughput: Math.floor(Math.random() * 400) + 200
    };
  }

  async getPerformanceMetrics(): Promise<PerformanceMetrics> {
    await this.simulateLatency();
    
    return {
      latency: Math.floor(Math.random() * 40) + 10,
      cacheHitRate: Math.random() * 0.3 + 0.7, // 70-100%
      throughput: Math.floor(Math.random() * 300) + 200,
      memoryUsage: Math.floor(Math.random() * 50) + 20
    };
  }

  // Real API methods for when backend is available
  async callBackend(endpoint: string, data?: any): Promise<any> {
    try {
      const response = await fetch(`${this.baseUrl}/api/${endpoint}`, {
        method: data ? 'POST' : 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        body: data ? JSON.stringify(data) : undefined,
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API call failed:', error);
      // Fall back to mock data
      return this.getMockResponse(endpoint, data);
    }
  }

  private getMockResponse(endpoint: string, data?: any): any {
    switch (endpoint) {
      case 'process-message':
        return this.processMessage(data?.message || '');
      case 'stats':
        return this.getStats();
      case 'performance':
        return this.getPerformanceMetrics();
      default:
        return null;
    }
  }
}

export const apiService = new ApiService();
