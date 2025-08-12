# 🚀 Context-Aware Conversational Ad Relevance Engine

**Powered by OpenAI ChatGPT - Dynamic AI Conversations with Intelligent Ad Suggestions**

A sophisticated Java-based system that integrates ChatGPT for natural conversations while intelligently suggesting relevant advertisements based on conversation context, user intent, and mood analysis.

## ✨ Features

### 🤖 **ChatGPT-Powered Conversations**
- **Natural AI Conversations**: ChatGPT engages in human-like conversations
- **Context Awareness**: Understands conversation history and user state
- **Dynamic Responses**: Generates contextual, intelligent responses
- **No Mock Data**: Uses only real ChatGPT API for all interactions

### 🎯 **Intelligent Ad Suggestions**
- **Contextual Relevance**: Suggests ads based on conversation topics
- **Intent Detection**: Analyzes user intent (shopping, travel, tech, etc.)
- **Mood Analysis**: Considers user mood for personalized suggestions
- **Clickable Links**: Ad suggestions include clickable HTML buttons

### ⚡ **Performance & Scalability**
- **Low Latency**: Optimized for real-time conversations
- **Asynchronous Processing**: Non-blocking ChatGPT API calls
- **Caching**: Intelligent response caching for better performance
- **High Throughput**: Handles multiple concurrent conversations

### 🔧 **Integration Ready**
- **REST API**: Full REST API for easy integration
- **OpenWebUI Support**: Ready-to-use OpenWebUI integration
- **Demo Interface**: Live demo page for testing
- **Docker Support**: Containerized deployment options

## 🏗️ Architecture

```mermaid
graph TB
    subgraph "Frontend Layer"
        UI[Demo UI]
        OWUI[OpenWebUI Integration]
    end
    
    subgraph "Integration Layer"
        IS[Integration Server]
        WS[WebSocket Server]
    end
    
    subgraph "Backend Layer"
        API[ChatGPT API]
        CE[ChatGPT Enhanced Engine]
        CS[ChatGPT Service]
        CM[Conversation Manager]
    end
    
    subgraph "External Services"
        OA[OpenAI API]
    end
    
    UI --> IS
    OWUI --> IS
    IS --> API
    API --> CE
    CE --> CS
    CS --> OA
    CE --> CM
    
    classDef aiClass fill:#ff6b6b,stroke:#333,stroke-width:2px,color:#fff
    class CE,CS aiClass
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 16+ (for integration server)
- OpenAI API Key

### 1. Clone and Setup
```bash
git clone https://github.com/yourusername/ConversationalAdRelevanceEngine.git
cd ConversationalAdRelevanceEngine
```

### 2. Configure OpenAI API
Edit `src/main/resources/application.properties`:
```properties
# Replace with your actual OpenAI API key
openai.api.key=your-openai-api-key-here
openai.api.url=https://api.openai.com/v1/chat/completions
openai.model=gpt-3.5-turbo
```

### 3. Start the Backend
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.adrelevance.api.ChatGPTAdRelevanceAPI"
```

### 4. Start the Integration Server
```bash
cd openwebui-integration
npm install
node server.js
```

### 5. Access the Demo
Open `http://localhost:3001` in your browser

## 🎯 Demo Scenarios

### **Conversational AI Response**
```
User: "Hello, how are you today?"
ChatGPT: "Hello! I'm here and ready to chat. How are you feeling today?"
```

### **Travel with Ad Suggestion**
```
User: "I want to visit London next month"
ChatGPT: "That sounds like an amazing trip! London is such a vibrant city with so much to see and do. Have you started planning your itinerary yet? [Book London Tours]"
```

### **Shopping with Ad Suggestion**
```
User: "I need a new dress for a party"
ChatGPT: "How exciting! Finding the perfect dress for a party can be so much fun. Do you have a specific style or color in mind? [Shop Party Dresses]"
```

### **General Conversation**
```
User: "What's the weather like?"
ChatGPT: "I don't have access to real-time weather data, but I'd be happy to help you plan your day or suggest some activities! What are you up to today?"
```

## 🔌 API Reference

### Process Message
```bash
POST /api/chatgpt/process-message
Content-Type: application/json

{
  "conversationId": "unique_conversation_id",
  "userId": "user_id",
  "message": "Your message here"
}
```

**Response:**
```json
{
  "conversationId": "unique_conversation_id",
  "message": "Your message here",
  "adSuggestion": {
    "ad": {
      "title": "Ad Title",
      "description": "Ad description",
      "conversationalTemplate": "ChatGPT response with <a href='url'>clickable link</a>",
      "categories": ["category"],
      "callToAction": "Action Text"
    },
    "relevanceScore": 0.9,
    "response": "Full response with HTML"
  },
  "userId": "user_id"
}
```

### Update User Preferences
```bash
POST /api/chatgpt/update-preferences
Content-Type: application/json

{
  "userId": "user_id",
  "interests": ["technology", "fashion", "travel"],
  "blockedCategories": ["gambling", "alcohol"]
}
```

### Get Statistics
```bash
GET /api/chatgpt/stats
```

### Health Check
```bash
GET /api/chatgpt/health
```

## 🛠️ Integration Examples

### OpenWebUI Integration
```javascript
const client = new AdRelevanceClient({
  baseURL: 'http://localhost:3001'
});

// Process message for ad suggestions
const adSuggestion = await client.processMessage("I need a new smartphone");
if (adSuggestion) {
  console.log("Ad suggestion:", adSuggestion);
}
```

### Direct API Integration
```bash
curl -X POST http://localhost:8080/api/chatgpt/process-message \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "test_conv",
    "userId": "test_user", 
    "message": "I want to visit Paris"
  }'
```

## 🔧 Configuration

### ChatGPT Settings
```properties
# OpenAI Configuration
openai.api.key=your-api-key
openai.api.url=https://api.openai.com/v1/chat/completions
openai.model=gpt-3.5-turbo

# Server Configuration
server.port=8080

# Logging
logging.level.com.adrelevance=DEBUG
```

### Integration Server Settings
```json
{
  "adEngine": {
    "host": "localhost",
    "port": 8080,
    "endpoint": "/api/chatgpt/process-message",
    "timeout": 5000
  },
  "adSettings": {
    "minRelevanceScore": 0.1,
    "responseTemplate": "💡 {adResponse}",
    "cacheEnabled": true,
    "cacheTTL": 30000
  }
}
```

## 🏃‍♂️ Performance

- **Response Time**: ~1.5-2.5 seconds for ChatGPT API calls
- **Concurrent Users**: Supports multiple simultaneous conversations
- **Caching**: Intelligent response caching reduces API calls
- **Scalability**: Horizontal scaling ready with load balancing

## 🔒 Security

- **API Key Protection**: Never commit API keys to version control
- **Input Validation**: All user inputs are validated and sanitized
- **Rate Limiting**: Built-in rate limiting for API protection
- **Error Handling**: Graceful error handling without exposing sensitive data

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **OpenAI**: For providing the ChatGPT API
- **Spring Boot**: For the robust Java framework
- **OpenWebUI**: For the integration framework
- **Community**: For feedback and contributions

---

**Built with ❤️ for intelligent, conversational advertising**
