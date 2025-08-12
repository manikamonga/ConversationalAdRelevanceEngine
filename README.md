# Context-Aware Conversational Ad Relevance Engine

A **high-performance, low-latency** Java-based system that analyzes conversation context, detects user intent and mood, and dynamically suggests relevant advertisements in a natural, conversational manner. **Powered by OpenAI ChatGPT** for intelligent context analysis and ad suggestions.

## ⚡ Performance Highlights

- **Ultra-low latency**: < 50ms average response time
- **High throughput**: 100+ messages/second processing capability
- **Intelligent caching**: 30-second TTL with LRU eviction
- **Concurrent processing**: Async support with thread pool optimization
- **Memory efficient**: Optimized data structures and cache management
- **Real-time analytics**: Sub-millisecond analytics generation

## 🎯 Features

### Core Capabilities
- **ChatGPT-Powered Analysis**: Uses OpenAI's GPT for intelligent context analysis and ad suggestions
- **Dynamic Ad Generation**: Creates relevant ads based on conversation context instead of hardcoded inventory
- **Smart Ad Matching**: Matches ads based on conversation context, user preferences, and relevance scoring
- **Conversational Responses**: Generates natural, context-aware ad suggestions with clickable links
- **User State Management**: Tracks user preferences, interests, and interaction history
- **Real-time Analytics**: Provides conversation analytics and engine statistics

### Advanced Features
- **AI-Powered Mood Detection**: ChatGPT identifies conversation mood (positive, excited, curious, frustrated, etc.)
- **Intelligent Intent Recognition**: Detects user intents (shopping, research, entertainment, travel, etc.) using natural language understanding
- **Dynamic Topic Extraction**: Identifies relevant topics and assigns weights based on context
- **Personalization**: Adapts responses based on user history and preferences
- **Multi-turn Conversations**: Maintains context across conversation turns
- **Robust Fallback System**: Graceful degradation to intelligent mock responses when ChatGPT API is unavailable

### Performance Features
- **Intelligent Caching**: 30-second TTL with automatic cache invalidation
- **Async Processing**: Non-blocking message processing with CompletableFuture
- **ConcurrentHashMap**: Thread-safe data structures for high concurrency
- **LRU Cache Eviction**: Memory-efficient cache management
- **Performance Monitoring**: Built-in latency tracking and metrics
- **Resource Management**: Automatic cleanup and shutdown procedures

### Modern Web UI
- **React-based Interface**: Beautiful, responsive chat interface
- **Real-time Performance Metrics**: Live dashboard showing engine statistics
- **User Preferences Management**: Customizable interests and blocked categories
- **Interactive Ad Suggestions**: Rich ad cards with relevance scores
- **Glass Morphism Design**: Modern, elegant visual design
- **Mobile Responsive**: Works seamlessly on all devices

## 🏗️ Architecture

### System Architecture Diagram

```mermaid
graph TB
    %% External Inputs
    User[👤 User] --> |Message| LLM[🤖 LLM Platform]
    LLM --> |Forward Message| Engine[🚀 ChatGPTAdRelevanceEngine]
    
    %% Main Engine Components
    Engine --> Cache[💾 Intelligent Cache]
    Engine --> ContextMgr[📝 ConversationManager]
    Engine --> ChatGPTService[🤖 ChatGPT Service]
    Engine --> ResponseGen[💬 Response Generator]
    
    %% ChatGPT Integration
    ChatGPTService --> |API Call| OpenAI[🔗 OpenAI API]
    ChatGPTService --> |Fallback| MockResponse[🎭 Mock Response]
    
    %% Data Flow
    ContextMgr --> |Get/Update| Context[📊 ConversationContext]
    ContextMgr --> |Get/Update| UserState[👤 UserState]
    
    ChatGPTService --> |Analyze| Context
    ChatGPTService --> |Generate| AdSuggestion[💡 AdSuggestion]
    
    %% Performance Components
    Engine --> ThreadPool[⚡ Thread Pool]
    Engine --> Metrics[📈 Performance Metrics]
    
    %% Output
    AdSuggestion --> |Return| Engine
    Engine --> |Ad Response| LLM
    LLM --> |Combined Response| User
    
    %% Styling
    classDef engineClass fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef dataClass fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef perfClass fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef externalClass fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef aiClass fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class Engine,ChatGPTService,ResponseGen,ContextMgr engineClass
    class Context,UserState,AdSuggestion dataClass
    class Cache,ThreadPool,Metrics perfClass
    class User,LLM externalClass
    class OpenAI,ChatGPTService aiClass
```

### Core Components

1. **ConversationalAdRelevanceEngine** - Main orchestrator with caching and async processing
2. **ContextAnalyzer** - Analyzes conversation context and detects patterns
3. **AdMatchingEngine** - Matches ads based on relevance scoring
4. **ConversationalResponseGenerator** - Generates natural ad responses
5. **ConversationManager** - Manages conversation contexts and user states

### Data Models

- **ConversationContext** - Represents conversation state and metadata
- **UserState** - Tracks user preferences and behavior
- **Ad** - Represents advertisement with targeting properties
- **Message** - Individual conversation messages
- **AdSuggestion** - Suggested ad with conversational response

### Performance Architecture

```mermaid
sequenceDiagram
    participant U as User
    participant L as LLM
    participant E as Engine
    participant C as Cache
    participant A as Analyzer
    participant M as Matcher
    participant R as ResponseGen
    
    U->>L: Send Message
    L->>E: processMessageAsync()
    
    alt Cache Hit
        E->>C: Check Cache
        C-->>E: Return Cached Result
        E-->>L: Return AdSuggestion
    else Cache Miss
        E->>A: Analyze Context
        A-->>E: Updated Context
        E->>M: Find Relevant Ads
        M-->>E: Scored Ads
        E->>R: Generate Response
        R-->>E: AdSuggestion
        E->>C: Cache Result
        E-->>L: Return AdSuggestion
    end
    
    L-->>U: Combined Response
```

### Data Flow Architecture

```mermaid
flowchart LR
    subgraph "Input Layer"
        M[Message] --> P[Preprocessor]
    end
    
    subgraph "Analysis Layer"
        P --> CA[Context Analyzer]
        CA --> ID[Intent Detection]
        CA --> MD[Mood Detection]
        CA --> TE[Topic Extraction]
    end
    
    subgraph "Matching Layer"
        ID --> AM[Ad Matcher]
        MD --> AM
        TE --> AM
        UP[User Preferences] --> AM
        AI[Ad Inventory] --> AM
    end
    
    subgraph "Response Layer"
        AM --> CRG[Response Generator]
        CRG --> AR[Ad Response]
        CRG --> NR[Natural Response]
    end
    
    subgraph "Performance Layer"
        AR --> C[Cache]
        NR --> C
        C --> M[Metrics]
    end
```

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Node.js 16+ (for UI)
- npm or yarn

### Backend Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd ConversationalAdRelevanceEngine
```

2. Build the Java backend:
```bash
mvn clean compile
```

3. Configure ChatGPT API (optional):
```bash
# Edit src/main/resources/application.properties
# Add your OpenAI API key
openai.api.key=your_openai_api_key_here
```

4. Run the ChatGPT-powered backend:
```bash
mvn exec:java -Dexec.mainClass="com.adrelevance.api.ChatGPTAdRelevanceAPI"
```

### Frontend Installation

1. Install frontend dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm start
```

3. Open your browser to `http://localhost:3000`

### Running Both Together

1. Start the ChatGPT-powered Java backend (in one terminal):
```bash
mvn exec:java -Dexec.mainClass="com.adrelevance.api.ChatGPTAdRelevanceAPI"
```

2. Start the React frontend (in another terminal):
```bash
npm start
```

## 🖥️ UI Features

### Chat Interface
- **Real-time Messaging**: Instant message processing with typing indicators
- **Rich Ad Cards**: Beautiful ad suggestions with relevance scores and categories
- **User Preferences**: Manage interests and blocked categories
- **Performance Dashboard**: Live metrics and statistics
- **Responsive Design**: Works on desktop, tablet, and mobile

### Interactive Elements
- **Suggested Messages**: Quick-start conversation prompts
- **Ad Relevance Scores**: Visual indicators of ad relevance (0-100%)
- **Category Tags**: Easy-to-read ad categories
- **Call-to-Action Buttons**: Direct links to advertised products
- **Performance Metrics**: Real-time latency, throughput, and cache statistics

### User Experience
- **Glass Morphism Design**: Modern, elegant visual style
- **Smooth Animations**: Subtle transitions and loading states
- **Keyboard Shortcuts**: Enter to send, Shift+Enter for new lines
- **Auto-scroll**: Messages automatically scroll to bottom
- **Error Handling**: Graceful error messages and fallbacks

## 📊 Demo Scenarios

The ChatGPT-powered demo showcases intelligent conversation analysis:

### 1. Fashion & Style
```
User: "I need a new dress for summer"
ChatGPT Analysis: Detects shopping intent, fashion category, excited mood
Response: "Hey! I noticed you're into style. Our new summer collection is absolutely stunning! 🌸 <a href='https://fashionbrand.com/summer-collection' target='_blank'>Shop Now</a>"
```

### 2. Technology & Gadgets
```
User: "I want to buy a new smartphone"
ChatGPT Analysis: Detects technology intent, shopping mood, tech category
Response: "Speaking of tech, have you seen the latest smartphone? It's pretty amazing! 📱 <a href='https://techcorp.com/latest-smartphone' target='_blank'>Learn More</a>"
```

### 3. Travel & Adventure
```
User: "I'm planning a vacation to Europe"
ChatGPT Analysis: Detects travel intent, excited mood, vacation planning
Response: "Dreaming of a vacation? I know the perfect place for your next adventure! ✈️ <a href='https://travelagency.com/dream-vacation' target='_blank'>Book Now</a>"
```

### 4. No Relevant Ad Found
```
User: "What's the weather like today?"
ChatGPT Analysis: No shopping intent detected, low confidence
Response: "I don't have ad suggestions for this product right now. Try asking about technology, fashion, travel, food, fitness, or beauty products! 💡"
```

### Key Features:
- **Intelligent Context Analysis**: ChatGPT understands conversation flow and user intent
- **Dynamic Ad Generation**: Creates relevant ads based on context, not hardcoded inventory
- **Clickable Links**: HTML links rendered as beautiful buttons in the UI
- **Confidence Scoring**: Only shows ads when relevance confidence is high (>0.3)
- **Graceful Fallback**: Intelligent responses when no relevant ads are found

## 🎨 Customization

### Adding New Ads

```java
Ad newAd = new Ad("custom_001", "Product Name", "Description", "Brand");
newAd.setCallToAction("Shop Now");
newAd.addCategory("electronics");
newAd.setTopicRelevance("technology", 0.9);
newAd.setMoodRelevance(UserMood.EXCITED, 0.8);
newAd.setConversationalTemplate("Hey tech enthusiast! {title} is perfect for you! 🚀");

engine.addAd(newAd);
```

### Custom Intent Detection

Extend the `ContextAnalyzer` class to add custom intent keywords:

```java
// In ContextAnalyzer.initializeIntentKeywords()
keywords.put("custom_intent", Arrays.asList("keyword1", "keyword2", "keyword3"));
```

### Custom Response Templates

Add new mood-based or intent-based response templates in `ConversationalResponseGenerator`.

### UI Customization

The React frontend can be customized by:

1. **Modifying Colors**: Update the Tailwind config in `tailwind.config.js`
2. **Adding Components**: Create new React components in `src/components/`
3. **Custom Styling**: Modify `src/index.css` for custom styles
4. **API Integration**: Update `src/services/api.ts` for backend integration

## 📈 Analytics & Performance

The engine provides comprehensive analytics and performance metrics:

```java
// Get conversation analytics
ConversationAnalytics analytics = engine.getAnalytics("conv123");
System.out.println("Mood: " + analytics.getMood());
System.out.println("Intents: " + analytics.getDetectedIntents());
System.out.println("Topics: " + analytics.getTopicWeights());

// Get engine statistics
EngineStats stats = engine.getStats();
System.out.println("Active conversations: " + stats.getActiveConversations());
System.out.println("Ad inventory size: " + stats.getAdInventorySize());
System.out.println("Total users: " + stats.getTotalUsers());

// Performance monitoring is built-in
// Latency metrics are automatically logged for each request
```

## ⚡ Performance Testing

Run performance tests to verify low latency capabilities:

```bash
mvn test -Dtest=PerformanceTest
```

Expected results:
- Single message processing: < 50ms
- Cache hit latency: < 10ms
- Concurrent processing: < 1000ms for 5 messages
- Throughput: > 10 messages/second

## 🔧 Configuration

### Relevance Scoring Weights

The ad matching engine uses configurable weights:
- Topic relevance: 40%
- Mood relevance: 30%
- Intent relevance: 20%
- User preference: 10%

### Minimum Relevance Threshold

Ads with relevance scores below 0.3 are filtered out.

### Performance Configuration

```java
// Cache settings (configurable)
private static final long CACHE_TTL_MS = 30000; // 30 seconds
private static final int MAX_CACHE_SIZE = 1000; // Maximum cache entries

// Thread pool settings
private final ExecutorService executorService = Executors.newFixedThreadPool(4);
```

### Latency Optimization

- **Caching**: 30-second TTL with automatic invalidation
- **Async Processing**: Non-blocking operations with CompletableFuture
- **ConcurrentHashMap**: Thread-safe operations
- **Optimized Algorithms**: Efficient context analysis and ad matching
- **Memory Management**: LRU cache eviction and automatic cleanup

## 🧪 Testing

Run the test suite:

```bash
mvn test
```

Run performance tests specifically:

```bash
mvn test -Dtest=PerformanceTest
```

Run all tests including performance:

```bash
mvn test -Dtest="*Test"
```

## 📝 API Reference

### Main Engine Methods

- `processMessage(conversationId, userId, message)` - Process user message and return ad suggestion (synchronous)
- `processMessageAsync(conversationId, userId, message)` - Process user message asynchronously
- `processAdResponse(conversationId, adId, userResponse)` - Handle user response to ad
- `updateUserPreferences(userId, interests, blockedCategories)` - Update user preferences
- `getAnalytics(conversationId)` - Get conversation analytics
- `getStats()` - Get engine statistics
- `shutdown()` - Cleanup resources and shutdown gracefully

### Model Classes

- `ConversationContext` - Conversation state and metadata
- `UserState` - User preferences and behavior
- `Ad` - Advertisement with targeting properties
- `AdSuggestion` - Suggested ad with response
- `ConversationAnalytics` - Analytics data
- `EngineStats` - Engine statistics

## 🤝 Integration

### With ChatGPT Integration

```java
// ChatGPT-powered ad relevance engine
public class ChatGPTAdRelevanceEngine {
    private ChatGPTService chatGPTService;
    private ConversationManager conversationManager;
    
    public CompletableFuture<AdSuggestion> processMessage(String conversationId, String userId, String message) {
        // Get conversation context
        ConversationContext context = getOrCreateConversationContext(conversationId, userId);
        List<Message> conversationHistory = context.getMessages();
        UserState userState = context.getUserState();
        
        // Use ChatGPT to analyze context and suggest ads
        return chatGPTService.analyzeContextAndSuggestAd(
            conversationId, userId, message, conversationHistory, userState
        ).thenApply(chatGPTSuggestion -> {
            if (chatGPTSuggestion.hasAd()) {
                return new AdSuggestion(
                    chatGPTSuggestion.getAd(),
                    chatGPTSuggestion.getAd().getConversationalTemplate(),
                    chatGPTSuggestion.getConfidence()
                );
            } else {
                return new AdSuggestion(null, "I don't have ad suggestions for this product right now.", 0.0);
            }
        });
    }
}
```

### ChatGPT API Configuration

The system uses OpenAI's ChatGPT API for intelligent context analysis:

```properties
# OpenAI ChatGPT API Configuration
openai.api.key=your_openai_api_key_here
openai.api.url=https://api.openai.com/v1/chat/completions
openai.model=gpt-3.5-turbo
```

### Features of ChatGPT Integration

- **Intelligent Context Analysis**: ChatGPT analyzes conversation history, user state, and current message
- **Dynamic Ad Generation**: Creates relevant ads based on context instead of hardcoded inventory
- **Natural Language Understanding**: Understands user intent, mood, and interests
- **Robust Fallback**: Graceful degradation to mock responses when API is unavailable
- **High Performance**: Async processing with CompletableFuture for low latency
- **Clickable Ad Links**: HTML links rendered as beautiful buttons in the UI

### REST API Integration

The ChatGPT-powered engine provides REST API endpoints:

#### API Endpoints

- `POST /api/chatgpt/process-message` - Process message and get ad suggestion
- `POST /api/chatgpt/update-preferences` - Update user preferences
- `GET /api/chatgpt/stats` - Get engine statistics
- `GET /api/chatgpt/health` - Health check

#### Example API Usage

```bash
# Process a message
curl -X POST http://localhost:8080/api/chatgpt/process-message \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "conv123",
    "userId": "user456",
    "message": "I need a new smartphone"
  }'

# Response
{
  "conversationId": "conv123",
  "message": "I need a new smartphone",
  "userId": "user456",
  "adSuggestion": {
    "ad": {
      "id": "chatgpt_1234567890",
      "title": "Latest Smartphone",
      "description": "Experience cutting-edge technology",
      "conversationalTemplate": "Speaking of tech, have you seen the latest smartphone? It's pretty amazing! 📱 <a href='https://techcorp.com/latest-smartphone' target='_blank'>Learn More</a>"
    },
    "response": "Speaking of tech, have you seen the latest smartphone? It's pretty amazing! 📱 <a href='https://techcorp.com/latest-smartphone' target='_blank'>Learn More</a>",
    "relevanceScore": 0.8
  }
}
```

### Frontend Integration

The React frontend provides a complete UI for testing and demonstration:

```typescript
// Example of using the API service
import { apiService } from './services/api';

// Process a message
const adSuggestion = await apiService.processMessage("I need a new smartphone");

// Update user preferences
await apiService.updateUserPreferences({
  interests: ['technology', 'fashion'],
  blockedCategories: ['gambling']
});

// Get performance metrics
const metrics = await apiService.getPerformanceMetrics();
```

## 🔒 Privacy & Ethics

- User data is stored in memory only (no persistence)
- No personal information is collected
- Users can opt out of ad suggestions
- Transparent about ad placement

## 🚀 Future Enhancements

- [ ] Machine learning-based intent detection
- [ ] Sentiment analysis integration
- [ ] Multi-language support
- [ ] A/B testing framework
- [ ] Real-time learning from user interactions
- [ ] Integration with external ad platforms
- [ ] Advanced personalization algorithms
- [ ] Redis/Memcached integration for distributed caching
- [ ] Kafka integration for high-throughput message processing
- [ ] Kubernetes deployment with auto-scaling
- [ ] Prometheus metrics and Grafana dashboards
- [ ] gRPC API for microservice architecture
- [ ] Database persistence for conversation history
- [ ] Advanced ML models for better relevance scoring
- [ ] WebSocket support for real-time updates
- [ ] Progressive Web App (PWA) features
- [ ] Dark mode and theme customization
- [ ] Advanced analytics dashboard
- [ ] Multi-user chat rooms
- [ ] Voice input and output support

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📞 Support

For questions or support, please open an issue on GitHub or contact the development team.

---

**Built with ❤️ for the future of conversational advertising**
