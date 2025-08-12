/**
 * OpenWebUI Ad Relevance Client
 * 
 * This client library provides easy integration with the Ad Relevance Engine
 * from within OpenWebUI or any other web application.
 */

class AdRelevanceClient {
  constructor(config = {}) {
    this.baseURL = config.baseURL || 'http://localhost:3001';
    this.wsURL = config.wsURL || 'ws://localhost:3002';
    this.sessionId = config.sessionId || this.generateSessionId();
    this.userId = config.userId || this.generateUserId();
    this.ws = null;
    this.callbacks = new Map();
    this.connected = false;
  }

  generateSessionId() {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  generateUserId() {
    return `user_${Math.random().toString(36).substr(2, 9)}`;
  }

  // REST API methods
  async processMessage(message) {
    try {
      const response = await fetch(`${this.baseURL}/api/process-message`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          sessionId: this.sessionId,
          userId: this.userId,
          message
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data.adSuggestion;
    } catch (error) {
      console.error('Failed to process message:', error);
      return null;
    }
  }

  async updatePreferences(preferences) {
    try {
      const response = await fetch(`${this.baseURL}/api/update-preferences`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          sessionId: this.sessionId,
          preferences
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data.success;
    } catch (error) {
      console.error('Failed to update preferences:', error);
      return false;
    }
  }

  async getStats() {
    try {
      const response = await fetch(`${this.baseURL}/api/stats`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Failed to get stats:', error);
      return null;
    }
  }

  async clearCache() {
    try {
      const response = await fetch(`${this.baseURL}/api/clear-cache`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data.success;
    } catch (error) {
      console.error('Failed to clear cache:', error);
      return false;
    }
  }

  // WebSocket methods
  connect() {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      this.ws = new WebSocket(this.wsURL);

      this.ws.onopen = () => {
        this.connected = true;
        console.log('Ad Relevance WebSocket connected');
        resolve();
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.handleWebSocketMessage(data);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        reject(error);
      };

      this.ws.onclose = () => {
        this.connected = false;
        console.log('Ad Relevance WebSocket disconnected');
      };
    });
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
      this.connected = false;
    }
  }

  handleWebSocketMessage(data) {
    const callback = this.callbacks.get(data.type);
    if (callback) {
      callback(data);
    }
  }

  on(event, callback) {
    this.callbacks.set(event, callback);
  }

  off(event) {
    this.callbacks.delete(event);
  }

  // WebSocket message processing
  async processMessageWS(message) {
    if (!this.connected) {
      await this.connect();
    }

    return new Promise((resolve, reject) => {
      const messageId = Date.now().toString();
      
      const timeout = setTimeout(() => {
        this.off(`ad-suggestion-${messageId}`);
        reject(new Error('WebSocket timeout'));
      }, 5000);

      this.on(`ad-suggestion-${messageId}`, (data) => {
        clearTimeout(timeout);
        this.off(`ad-suggestion-${messageId}`);
        resolve(data.adSuggestion);
      });

      this.ws.send(JSON.stringify({
        type: 'process-message',
        sessionId: this.sessionId,
        message,
        messageId
      }));
    });
  }

  // Utility methods
  async healthCheck() {
    try {
      const response = await fetch(`${this.baseURL}/health`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Health check failed:', error);
      return null;
    }
  }

  // OpenWebUI specific integration
  integrateWithOpenWebUI() {
    // This method provides hooks for OpenWebUI integration
    // It can be customized based on OpenWebUI's specific requirements
    
    const originalSendMessage = window.sendMessage || function() {};
    
    window.sendMessage = async (message, options = {}) => {
      // Send the original message
      const result = await originalSendMessage(message, options);
      
      // Process for ad suggestions
      try {
        const adSuggestion = await this.processMessage(message);
        
        if (adSuggestion) {
          // Inject ad suggestion into the chat
          this.injectAdSuggestion(adSuggestion);
        }
      } catch (error) {
        console.error('Failed to process ad suggestion:', error);
      }
      
      return result;
    };

    console.log('Ad Relevance Client integrated with OpenWebUI');
  }

  injectAdSuggestion(adSuggestion) {
    // This method injects ad suggestions into the OpenWebUI chat interface
    // The implementation depends on OpenWebUI's DOM structure
    
    const chatContainer = document.querySelector('.chat-container') || 
                         document.querySelector('.messages') ||
                         document.querySelector('[data-testid="chat-messages"]');
    
    if (chatContainer) {
      const adElement = document.createElement('div');
      adElement.className = 'ad-suggestion-message';
      adElement.innerHTML = `
        <div class="ad-suggestion" style="
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          padding: 12px 16px;
          border-radius: 12px;
          margin: 8px 0;
          border-left: 4px solid #4ade80;
          font-size: 14px;
          line-height: 1.4;
        ">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
            <span style="font-size: 16px;">💡</span>
            <span style="font-weight: 600;">Ad Suggestion</span>
          </div>
          <div style="margin-bottom: 8px;">
            ${adSuggestion}
          </div>
          <div style="display: flex; gap: 8px; font-size: 12px; opacity: 0.8;">
            <span>Context-aware advertising</span>
            <span>•</span>
            <span>Powered by Ad Relevance Engine</span>
          </div>
        </div>
      `;
      
      chatContainer.appendChild(adElement);
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }
  }

  // Configuration methods
  setSessionId(sessionId) {
    this.sessionId = sessionId;
  }

  setUserId(userId) {
    this.userId = userId;
  }

  setBaseURL(baseURL) {
    this.baseURL = baseURL;
  }

  setWsURL(wsURL) {
    this.wsURL = wsURL;
  }
}

// Export for different module systems
if (typeof module !== 'undefined' && module.exports) {
  module.exports = AdRelevanceClient;
} else if (typeof window !== 'undefined') {
  window.AdRelevanceClient = AdRelevanceClient;
}

// Auto-initialization for browser usage
if (typeof window !== 'undefined') {
  window.adRelevanceClient = new AdRelevanceClient();
  
  // Auto-integrate with OpenWebUI if detected
  if (window.location.hostname === 'localhost' && window.location.port === '3000') {
    // This is a basic detection - you might want to make it more robust
    setTimeout(() => {
      if (document.querySelector('.chat-container') || 
          document.querySelector('.messages')) {
        window.adRelevanceClient.integrateWithOpenWebUI();
      }
    }, 2000);
  }
}
