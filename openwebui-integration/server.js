const express = require('express');
const cors = require('cors');
const WebSocket = require('ws');
const axios = require('axios');
const cron = require('node-cron');
const winston = require('winston');
const path = require('path');
const fs = require('fs');

// Load configuration
const config = JSON.parse(fs.readFileSync('./config.json', 'utf8'));

// Setup logging
const logger = winston.createLogger({
  level: config.logging.level,
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'openwebui-ad-integration' },
  transports: [
    new winston.transports.File({ 
      filename: config.logging.file,
      maxsize: config.logging.maxSize,
      maxFiles: config.logging.maxFiles
    }),
    new winston.transports.Console({
      format: winston.format.simple()
    })
  ]
});

const app = express();
const PORT = config.integration.port;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// In-memory storage for sessions and cache
const sessions = new Map();
const adCache = new Map();
const analytics = {
  totalRequests: 0,
  adSuggestions: 0,
  averageRelevanceScore: 0,
  responseTimes: []
};

// Ad Relevance Engine client
class AdEngineClient {
  constructor() {
    this.baseURL = `http://${config.adEngine.host}:${config.adEngine.port}`;
    this.timeout = config.adEngine.timeout;
  }

  async processMessage(conversationId, userId, message) {
    try {
      const startTime = Date.now();
      
      const response = await axios.post(`${this.baseURL}${config.adEngine.endpoint}`, {
        conversationId,
        userId,
        message
      }, {
        timeout: this.timeout
      });

      const responseTime = Date.now() - startTime;
      analytics.responseTimes.push(responseTime);
      
      // Keep only last 100 response times
      if (analytics.responseTimes.length > 100) {
        analytics.responseTimes.shift();
      }

      logger.info('Ad engine response', {
        conversationId,
        userId,
        responseTime,
        hasAdSuggestion: !!response.data.adSuggestion
      });

      return response.data;
    } catch (error) {
      logger.error('Ad engine request failed', {
        error: error.message,
        conversationId,
        userId
      });
      return null;
    }
  }

  async updateUserPreferences(userId, preferences) {
    try {
      await axios.post(`${this.baseURL}/api/update-preferences`, {
        userId,
        ...preferences
      }, {
        timeout: this.timeout
      });
      
      logger.info('User preferences updated', { userId, preferences });
    } catch (error) {
      logger.error('Failed to update user preferences', {
        error: error.message,
        userId
      });
    }
  }
}

const adEngine = new AdEngineClient();

// Session management
class SessionManager {
  constructor() {
    this.sessions = new Map();
  }

  createSession(sessionId, userId) {
    const session = {
      id: sessionId,
      userId: userId,
      conversationId: `conv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      preferences: {
        interests: [...config.userPreferences.defaultInterests],
        blockedCategories: [...config.userPreferences.defaultBlockedCategories],
        adPreferencesEnabled: config.userPreferences.adPreferencesEnabled
      },
      messageHistory: [],
      createdAt: new Date()
    };

    this.sessions.set(sessionId, session);
    logger.info('Session created', { sessionId, userId });
    return session;
  }

  getSession(sessionId) {
    return this.sessions.get(sessionId);
  }

  updateSession(sessionId, updates) {
    const session = this.sessions.get(sessionId);
    if (session) {
      Object.assign(session, updates);
      this.sessions.set(sessionId, session);
    }
    return session;
  }

  addMessage(sessionId, message) {
    const session = this.sessions.get(sessionId);
    if (session) {
      session.messageHistory.push({
        content: message,
        timestamp: new Date(),
        type: 'user'
      });
      
      // Keep only last 50 messages
      if (session.messageHistory.length > 50) {
        session.messageHistory.shift();
      }
    }
  }
}

const sessionManager = new SessionManager();

// Ad suggestion processor
class AdSuggestionProcessor {
  constructor() {
    this.cache = new Map();
  }

  async processMessage(sessionId, message) {
    if (!config.adSettings.enabled) {
      return null;
    }

    const session = sessionManager.getSession(sessionId);
    if (!session || !session.preferences.adPreferencesEnabled) {
      return null;
    }

    // Check cache first
    const cacheKey = `${sessionId}_${message}`;
    if (config.adSettings.cacheEnabled && this.cache.has(cacheKey)) {
      const cached = this.cache.get(cacheKey);
      if (Date.now() - cached.timestamp < config.adSettings.cacheTTL) {
        logger.info('Cache hit for ad suggestion', { sessionId, cacheKey });
        return cached.suggestion;
      }
    }

    // Process with ad engine
    const result = await adEngine.processMessage(
      session.conversationId,
      session.userId,
      message
    );

    if (result && result.adSuggestion && result.adSuggestion.relevanceScore >= config.adSettings.minRelevanceScore) {
      const suggestion = this.formatAdSuggestion(result.adSuggestion);
      
      // Cache the result
      if (config.adSettings.cacheEnabled) {
        this.cache.set(cacheKey, {
          suggestion,
          timestamp: Date.now()
        });
      }

      analytics.adSuggestions++;
      analytics.totalRequests++;
      
      // Update average relevance score
      const totalScore = analytics.averageRelevanceScore * (analytics.adSuggestions - 1) + result.adSuggestion.relevanceScore;
      analytics.averageRelevanceScore = totalScore / analytics.adSuggestions;

      logger.info('Ad suggestion generated', {
        sessionId,
        relevanceScore: result.adSuggestion.relevanceScore,
        adTitle: result.adSuggestion.ad.title
      });

      return suggestion;
    }

    analytics.totalRequests++;
    return null;
  }

  formatAdSuggestion(adSuggestion) {
    const template = config.adSettings.responseTemplate;
    return template.replace('{adResponse}', adSuggestion.response);
  }

  clearCache() {
    this.cache.clear();
    logger.info('Ad suggestion cache cleared');
  }
}

const adProcessor = new AdSuggestionProcessor();

// REST API endpoints
app.post('/api/process-message', async (req, res) => {
  try {
    const { sessionId, message, userId } = req.body;
    
    if (!sessionId || !message) {
      return res.status(400).json({ error: 'Missing sessionId or message' });
    }

    // Get or create session
    let session = sessionManager.getSession(sessionId);
    if (!session) {
      session = sessionManager.createSession(sessionId, userId);
    }

    // Add message to session
    sessionManager.addMessage(sessionId, message);

    // Process for ad suggestions
    const adSuggestion = await adProcessor.processMessage(sessionId, message);

    res.json({
      sessionId,
      adSuggestion,
      session: {
        conversationId: session.conversationId,
        preferences: session.preferences
      }
    });
  } catch (error) {
    logger.error('Error processing message', { error: error.message });
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.post('/api/update-preferences', async (req, res) => {
  try {
    const { sessionId, preferences } = req.body;
    
    if (!sessionId) {
      return res.status(400).json({ error: 'Missing sessionId' });
    }

    const session = sessionManager.getSession(sessionId);
    if (!session) {
      return res.status(404).json({ error: 'Session not found' });
    }

    // Update session preferences
    sessionManager.updateSession(sessionId, {
      preferences: { ...session.preferences, ...preferences }
    });

    // Update ad engine preferences
    await adEngine.updateUserPreferences(session.userId, preferences);

    res.json({ success: true, session: sessionManager.getSession(sessionId) });
  } catch (error) {
    logger.error('Error updating preferences', { error: error.message });
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/stats', (req, res) => {
  const avgResponseTime = analytics.responseTimes.length > 0 
    ? analytics.responseTimes.reduce((a, b) => a + b, 0) / analytics.responseTimes.length 
    : 0;

  res.json({
    analytics: {
      ...analytics,
      averageResponseTime: Math.round(avgResponseTime),
      cacheSize: adProcessor.cache.size,
      activeSessions: sessionManager.sessions.size
    },
    config: {
      adSettings: config.adSettings,
      integration: config.integration
    }
  });
});

app.post('/api/clear-cache', (req, res) => {
  adProcessor.clearCache();
  res.json({ success: true, message: 'Cache cleared' });
});

// WebSocket server for real-time communication
let wss;
if (config.integration.enableWebSocket) {
  wss = new WebSocket.Server({ port: config.integration.port + 1 });
  
  wss.on('connection', (ws, req) => {
    logger.info('WebSocket client connected', { 
      remoteAddress: req.socket.remoteAddress 
    });

    ws.on('message', async (message) => {
      try {
        const data = JSON.parse(message);
        
        if (data.type === 'process-message') {
          const adSuggestion = await adProcessor.processMessage(
            data.sessionId, 
            data.message
          );
          
          ws.send(JSON.stringify({
            type: 'ad-suggestion',
            sessionId: data.sessionId,
            adSuggestion
          }));
        }
      } catch (error) {
        logger.error('WebSocket message error', { error: error.message });
        ws.send(JSON.stringify({
          type: 'error',
          error: 'Failed to process message'
        }));
      }
    });

    ws.on('close', () => {
      logger.info('WebSocket client disconnected');
    });
  });
}

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    version: '1.0.0'
  });
});

// Cache cleanup job (every 5 minutes)
cron.schedule('*/5 * * * *', () => {
  const now = Date.now();
  for (const [key, value] of adProcessor.cache.entries()) {
    if (now - value.timestamp > config.adSettings.cacheTTL) {
      adProcessor.cache.delete(key);
    }
  }
  logger.debug('Cache cleanup completed', { 
    remainingEntries: adProcessor.cache.size 
  });
});

// Start server
app.listen(PORT, () => {
  logger.info(`OpenWebUI Ad Integration Server running on port ${PORT}`);
  logger.info(`WebSocket server running on port ${PORT + 1}`);
  logger.info(`Health check available at http://localhost:${PORT}/health`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  logger.info('SIGTERM received, shutting down gracefully');
  if (wss) {
    wss.close();
  }
  process.exit(0);
});

process.on('SIGINT', () => {
  logger.info('SIGINT received, shutting down gracefully');
  if (wss) {
    wss.close();
  }
  process.exit(0);
});

module.exports = { app, adProcessor, sessionManager, analytics };
