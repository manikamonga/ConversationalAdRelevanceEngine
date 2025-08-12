# OpenWebUI Integration for Conversational Ad Relevance Engine

This integration allows you to use the Conversational Ad Relevance Engine alongside OpenWebUI to provide context-aware ad suggestions during conversations with LLMs.

## 🎯 Features

- **Seamless Integration**: Works with any OpenWebUI setup
- **Context-Aware Ads**: Analyzes conversation context to suggest relevant ads
- **Real-time Processing**: Low-latency ad matching and response generation
- **Customizable**: Easy to configure ad inventory and matching rules
- **Non-Intrusive**: Ads are suggested naturally within the conversation flow

## 🏗️ Architecture

```
OpenWebUI → Custom Extension → Ad Relevance Engine → Ad Suggestions
     ↓              ↓                    ↓              ↓
User Message → Context Analysis → Ad Matching → Natural Response
```

## 📦 Installation

### Prerequisites
- OpenWebUI installed and running
- Java 11+ (for the Ad Relevance Engine)
- Node.js 16+ (for the integration layer)

### 1. Install the Ad Relevance Engine

```bash
# Clone the repository
git clone https://github.com/manikamonga/ConversationalAdRelevanceEngine.git
cd ConversationalAdRelevanceEngine

# Build the Java backend
mvn clean compile

# Start the engine (optional - can be started by the integration)
mvn exec:java -Dexec.mainClass="com.adrelevance.demo.ConversationalAdDemo"
```

### 2. Install the OpenWebUI Integration

```bash
cd openwebui-integration
npm install
```

### 3. Configure the Integration

Edit `config.json`:

```json
{
  "adEngine": {
    "host": "localhost",
    "port": 8080,
    "endpoint": "/api/process-message"
  },
  "openwebui": {
    "host": "localhost",
    "port": 3000
  },
  "adSettings": {
    "enabled": true,
    "minRelevanceScore": 0.3,
    "maxAdsPerResponse": 1,
    "responseTemplate": "💡 {adResponse}"
  }
}
```

## 🚀 Usage

### Start the Integration Server

```bash
npm start
```

### Access OpenWebUI

1. Open your browser to `http://localhost:3000` (OpenWebUI)
2. Start a conversation with any model
3. The integration will automatically analyze your messages and suggest relevant ads

### Example Conversation

```
User: "I need a new smartphone for work"
OpenWebUI: "I can help you find a good smartphone for work! Here are some considerations..."

💡 Speaking of technology, have you seen the Latest Smartphone? It's pretty amazing! ✨
[Ad Card: Latest Smartphone - TechCorp - Shop Now]
```

## ⚙️ Configuration

### Ad Engine Settings

- **minRelevanceScore**: Minimum score (0-1) for ads to be suggested
- **maxAdsPerResponse**: Maximum number of ads to show per response
- **responseTemplate**: Template for ad responses

### User Preferences

Users can customize their experience:

```json
{
  "interests": ["technology", "fashion", "travel"],
  "blockedCategories": ["gambling", "alcohol"],
  "adPreferencesEnabled": true
}
```

## 🔧 Customization

### Adding Custom Ads

Edit `ads.json`:

```json
[
  {
    "id": "custom_001",
    "title": "Your Product",
    "description": "Product description",
    "brandName": "Your Brand",
    "categories": ["technology", "electronics"],
    "keywords": ["smartphone", "tech", "gadget"],
    "callToAction": "Learn More"
  }
]
```

### Custom Response Templates

Modify `responseTemplates.js`:

```javascript
module.exports = [
  "💡 {adResponse}",
  "By the way, {adResponse}",
  "Speaking of {category}, {adResponse}",
  "I thought you might be interested in {adResponse}"
];
```

## 📊 Monitoring

The integration provides real-time metrics:

- **Ad Suggestions**: Number of ads suggested
- **Relevance Scores**: Average relevance of suggested ads
- **Response Time**: Latency of ad matching
- **User Engagement**: Click-through rates on ads

## 🛠️ Development

### Local Development

```bash
# Start the ad engine
mvn exec:java -Dexec.mainClass="com.adrelevance.demo.ConversationalAdDemo"

# Start the integration in development mode
npm run dev

# Start OpenWebUI
# (Follow OpenWebUI installation instructions)
```

### API Endpoints

The integration exposes these endpoints:

- `POST /api/process-message` - Process a message and get ad suggestions
- `GET /api/stats` - Get engine statistics
- `POST /api/update-preferences` - Update user preferences
- `GET /api/ads` - Get available ads

## 🔒 Privacy & Ethics

- User data is processed locally
- No personal information is stored
- Users can opt out of ad suggestions
- Transparent about ad placement
- Respects user privacy preferences

## 🚀 Deployment

### Docker Deployment

```bash
# Build the integration
docker build -t ad-relevance-integration .

# Run with OpenWebUI
docker-compose up -d
```

### Production Setup

1. Deploy the Java backend to a production server
2. Configure the integration with production endpoints
3. Set up monitoring and logging
4. Configure SSL certificates
5. Set up user authentication

## 📝 License

This integration is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📞 Support

For questions or support, please open an issue on GitHub or contact the development team.
