export interface Message {
  id: string;
  content: string;
  sender: 'user' | 'bot';
  timestamp: Date;
  type: 'message' | 'ad-suggestion' | 'system';
  adSuggestion?: AdSuggestion;
}

export interface AdSuggestion {
  ad: Ad;
  response: string;
  relevanceScore: number;
}

export interface Ad {
  id: string;
  title: string;
  description: string;
  brandName: string;
  callToAction: string;
  categories: string[];
  type: string;
  relevanceScore: number;
}

export interface ConversationContext {
  conversationId: string;
  userId: string;
  messages: Message[];
  mood: string;
  detectedIntents: string[];
  topicWeights: Record<string, number>;
}

export interface UserState {
  userId: string;
  interests: string[];
  topicPreferences: Record<string, number>;
  currentMood: string;
  adPreferencesEnabled: boolean;
  blockedCategories: string[];
}

export interface EngineStats {
  activeConversations: number;
  adInventorySize: number;
  totalUsers: number;
  averageLatency: number;
  throughput: number;
}

export interface PerformanceMetrics {
  latency: number;
  cacheHitRate: number;
  throughput: number;
  memoryUsage: number;
}

export interface ChatState {
  messages: Message[];
  isLoading: boolean;
  conversationId: string;
  userId: string;
  userPreferences: UserState;
  stats: EngineStats;
  performance: PerformanceMetrics;
}
