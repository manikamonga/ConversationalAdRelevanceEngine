// OpenWebUI Ad Relevance Extension
// This extension integrates the Conversational Ad Relevance Engine with OpenWebUI

class OpenWebUIAdExtension {
    constructor() {
        this.adEngineUrl = 'http://localhost:8080/api/chatgpt/process-message';
        this.enabled = true;
        this.minRelevanceScore = 0.3;
        this.sessionId = null;
        this.userId = 'openwebui_user';
        this.processingMessages = new Set(); // Track messages being processed
        
        this.init();
    }

    init() {
        console.log('🚀 OpenWebUI Ad Relevance Extension initialized');
        this.setupMessageObserver();
        // Immediate scan for existing assistant messages
        this.scanForAssistantMessages();
        // Periodic scan as a safety net (covers DOM variants)
        this.scanTimer = setInterval(() => this.scanForAssistantMessages(), 2000);
        this.setupUI();
    }

    setupMessageObserver() {
        // Observe for new messages in the chat
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.type === 'childList') {
                    mutation.addedNodes.forEach((node) => {
                        if (node.nodeType === Node.ELEMENT_NODE) {
                            this.checkForNewMessages(node);
                        }
                    });
                }
            });
        });

        // Start observing the chat container; if not found, observe the whole document
        const chatContainer = document.querySelector('[data-testid="chat-messages"]') ||
                              document.querySelector('[data-testid="messages"]') ||
                              document.querySelector('[data-message-container]') ||
                              document.querySelector('.chat-messages') ||
                              document.querySelector('[class*="messages"]');

        if (chatContainer) {
            observer.observe(chatContainer, { childList: true, subtree: true });
        } else {
            console.warn('[Ads] Chat container not found. Observing document.body as fallback.');
            observer.observe(document.body, { childList: true, subtree: true });
        }
    }

    checkForNewMessages(node) {
        // Look for assistant messages that don't have ad suggestions yet
        const assistantMessages = node.querySelectorAll('[data-message-role="assistant"], [data-testid*="assistant"], [data-testid="assistant-message"], [data-testid="message"][data-role="assistant"], .assistant, .assistant-message, [class*="assistant"]');
        
        assistantMessages.forEach(message => {
            if (!message.hasAttribute('data-ad-processed')) {
                this.processAssistantMessage(message);
            }
        });
    }

    // Periodic scanner to catch messages even if MutationObserver misses them
    scanForAssistantMessages() {
        const nodes = document.querySelectorAll('[data-message-role="assistant"], [data-testid*="assistant"], [data-testid="assistant-message"], [data-testid="message"][data-role="assistant"], .assistant, .assistant-message, [class*="assistant"]');
        nodes.forEach((el) => {
            if (!el.hasAttribute('data-ad-processed')) {
                this.processAssistantMessage(el);
            }
        });
    }

    async processAssistantMessage(messageElement) {
        if (!this.enabled) return;

        // Mark as processed to avoid duplicate processing
        messageElement.setAttribute('data-ad-processed', 'true');

        // Skip generic greetings and non-commercial messages
        const messageText = messageElement.textContent?.trim() || '';
        if (this.shouldSkipMessage(messageText)) {
            return;
        }

        // Get the conversation context
        const conversationContext = this.getConversationContext();
        if (!conversationContext) return;

        try {
            // Call our ad relevance engine
            const adSuggestion = await this.getAdSuggestion(conversationContext);
            
            // Only show ads for high-confidence commercial intent
            if (adSuggestion && adSuggestion.adSuggestion && adSuggestion.adSuggestion.relevanceScore >= 0.6) {
                this.displayAdSuggestion(messageElement, adSuggestion);
            }
        } catch (error) {
            console.error('Error processing ad suggestion:', error);
        }
    }

    shouldSkipMessage(messageText) {
        const skipPatterns = [
            /hello!? how can i (assist|help) you today\??/i,
            /^(hi|hello|hey)( there)?!?/i,
            /^(what|how|when|where|why|who)\s/i,
            /^(explain|tell me about|describe)/i,
            /^(thank you|thanks|bye|goodbye)/i,
            /^(yes|no|maybe|ok|okay)\s*$/i,
            /\b(weather|time|date|calendar)\b/i,
            /\b(math|calculate|solve|equation)\b/i,
            /^(i don't know|i'm not sure)/i,
            /^(that's interesting|cool|nice)/i
        ];
        
        return skipPatterns.some(pattern => pattern.test(messageText.trim()));
    }

    isObviouslyNonCommercial(messageText) {
        const nonCommercialPatterns = [
            /^(what|how|when|where|why|who)\s/i,
            /^(explain|tell me about|describe)/i,
            /\b(weather|time|date|calendar)\b/i,
            /^(thank you|thanks|bye|goodbye)/i,
            /^(yes|no|maybe|ok|okay)\s*$/i,
            /\b(math|calculate|solve|equation)\b/i
        ];
        
        return nonCommercialPatterns.some(pattern => pattern.test(messageText.trim()));
    }

    isGenericGreeting(messageText) {
        const greetings = [
            'hello! how can i assist you today?',
            'hello! how can i help you today?',
            'hi! how can i assist you today?',
            'hi! how can i help you today?',
            'how can i assist you today?',
            'how can i help you today?',
            'hello there!',
            'hi there!',
            'greetings!',
            'welcome!',
            'sure, here are a few',
            'here are some options',
            'here are a few options',
            'please note that prices can vary',
            'i can help you',
            'i\'d be happy to help',
            'let me help you',
            'here are some suggestions',
            'here are a few suggestions',
            'i recommend',
            'you might want to consider',
            'some options include',
            'a few options include'
        ];
        return greetings.some(greeting => messageText.toLowerCase().includes(greeting.toLowerCase()));
    }

    getConversationContext() {
        // Extract recent messages from the chat
        const messages = [];
        const messageElements = document.querySelectorAll('[data-message-role="assistant"], [data-message-role="user"], [data-testid="message"], article, li, .message, [class*="message"]');
        
        // Get the last few messages for context
        const recentMessages = Array.from(messageElements).slice(-6);
        
        recentMessages.forEach(element => {
            const roleAttr = element.getAttribute('data-message-role');
            const isUser = (roleAttr === 'user') ||
                           (element.hasAttribute('data-testid') && element.getAttribute('data-testid').includes('user')) ||
                           element.classList.contains('user-message') ||
                           element.classList.contains('user');
            
            const content = element.textContent?.trim();
            if (content) {
                messages.push({
                    role: isUser ? 'user' : 'assistant',
                    content: content
                });
            }
        });

        // Pick the most recent USER message as the trigger
        let lastUserMessage = '';
        for (let i = messages.length - 1; i >= 0; i -= 1) {
            if (messages[i].role === 'user') { lastUserMessage = messages[i].content; break; }
        }

        return {
            messages: messages,
            lastUserMessage,
            conversationId: this.getConversationId(),
            userId: this.userId
        };
    }

    getConversationId() {
        // Try to extract conversation ID from URL or page elements
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('id') || 
               document.querySelector('[data-conversation-id]')?.getAttribute('data-conversation-id') ||
               `conv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    }

    async getAdSuggestion(context) {
        const requestBody = {
            conversationId: context.conversationId,
            userId: context.userId,
            // Always send the last USER message so we don't analyze assistant replies
            message: context.lastUserMessage || ''
        };

        if (!requestBody.message) {
            return { adSuggestion: null };
        }

        const response = await fetch(this.adEngineUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    }

    displayAdSuggestion(messageElement, adSuggestion) {
        const sug = adSuggestion && adSuggestion.adSuggestion;
        if (!sug) return;

        // Only show ads for high-confidence commercial intent
        if (sug.relevanceScore < 0.6) return;

        // Suppress non-ad conversational placeholders
        const meta = (sug.ad || {});
        const titleLc = (meta.title || '').toLowerCase();
        const catsLc = Array.isArray(meta.categories) ? meta.categories.map(c => (c || '').toLowerCase()) : [];
        if (titleLc === 'conversational response' || catsLc.includes('conversation')) {
            return; // do not render any ad banner
        }

        // Build a compact one-line inline ad from either server HTML or fields
        let url = '';
        let text = '';
        if (sug.response) {
            const responseText = sug.response;
            const hrefMatch = responseText.match(/<a[^>]+href="([^"]+)"[^>]*>([\s\S]*?)<\/a>/i);
            if (hrefMatch) {
                url = hrefMatch[1];
                text = (hrefMatch[2] || '').replace(/<[^>]+>/g, '').trim();
            }
        }

        if (!url) {
            // Try to use structured fields if available
            const ad = sug.ad || {};
            if (ad.url && ad.title) {
                url = ad.url;
                text = ad.title;
            }
        }

        if (!url || !/^https?:\/\//i.test(url)) return; // nothing to render
        if (!text) text = 'Learn more';

        // Render a single-line inline ad with a Sponsored pill and clickable link
        const inline = document.createElement('div');
        inline.className = 'openwebui-inline-ad';
        inline.innerHTML = `
            <span class="sponsored-pill">Sponsored</span>
            <a class="sponsored-link-inline" href="${url}" target="_blank" rel="noopener noreferrer">${text}</a>
        `;
        this.addAdBannerStyles();
        messageElement.appendChild(inline);
        return;
    }

    createAdBannerHTML(ad) {
        return `
            <div class="ad-banner-container">
                <div class="ad-banner">
                    <div class="ad-banner-header">
                        <span class="ad-label">💡 Sponsored Suggestion</span>
                    </div>
                    <div class="ad-banner-content">
                        <div class="ad-icon">💡</div>
                        <div class="ad-text">
                            <h4 class="ad-title">${ad.title}</h4>
                            <p class="ad-description">${ad.description}</p>
                        </div>
                    </div>
                    <div class="ad-banner-actions">
                        <a href="#" class="ad-button" data-ad-url="${ad.url || ''}">
                            ${ad.callToAction || 'Learn More'}
                        </a>
                    </div>
                </div>
            </div>
        `;
    }

    addAdBannerStyles() {
        if (document.getElementById('openwebui-ad-styles')) return;

        const styles = document.createElement('style');
        styles.id = 'openwebui-ad-styles';
        styles.textContent = `
            .openwebui-inline-ad { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #1f2937; margin: 4px 0; }
            .openwebui-inline-ad .sponsored-pill { background: #f3f4f6; color: #6b7280; padding: 2px 6px; border-radius: 999px; font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: .3px; }
            .openwebui-inline-ad .sponsored-link-inline { color: #1f2937; text-decoration: none; font-weight: 600; }
            .openwebui-inline-ad .sponsored-link-inline:hover { color: #3b82f6; text-decoration: underline; }

            /* Legacy banner styles retained but unused */
            .openwebui-ad-banner { margin-top: 6px; margin-bottom: 4px; }
            
            .openwebui-ad-banner .sponsored-banner {
                background: white;
                border: 1px solid #e2e8f0;
                border-radius: 8px;
                padding: 12px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                position: relative;
                overflow: hidden;
                font-size: 12px;
            }
            
            .openwebui-ad-banner .sponsored-banner h4 {
                margin: 0 0 4px 0;
                color: #1f2937;
                font-size: 14px;
                font-weight: 600;
            }
            
            .openwebui-ad-banner .sponsored-banner p {
                margin: 0 0 8px 0;
                color: #6b7280;
                font-size: 12px;
                line-height: 1.4;
            }
            
            .openwebui-ad-banner .sponsored-banner a {
                display: inline-block;
                background: #3b82f6;
                color: white;
                padding: 6px 12px;
                border-radius: 6px;
                text-decoration: none;
                font-weight: 600;
                font-size: 12px;
                transition: all 0.2s;
                border: none;
            }
            
            .openwebui-ad-banner .sponsored-banner a:hover {
                background: #2563eb;
                color: white;
                text-decoration: none;
                transform: translateY(-1px);
            }
            
            .ad-banner-container {
                background: white;
                border: 1px solid #e2e8f0;
                border-radius: 8px;
                padding: 12px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                position: relative;
                overflow: hidden;
                transition: transform 0.2s ease;
            }
            
            .ad-banner-container:hover {
                transform: translateY(-1px);
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
            }
            
            .ad-banner-header {
                position: absolute;
                top: 8px;
                right: 8px;
            }
            
            .ad-label {
                background: #f3f4f6;
                color: #6b7280;
                padding: 2px 8px;
                border-radius: 12px;
                font-size: 10px;
                font-weight: 600;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }
            
            .ad-banner-content {
                display: flex;
                align-items: center;
                gap: 12px;
                margin-bottom: 12px;
            }
            
            .ad-icon {
                width: 40px;
                height: 40px;
                background: rgba(255, 255, 255, 0.2);
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 18px;
            }
            
            .ad-text {
                flex: 1;
            }
            
            .ad-title {
                margin: 0 0 4px 0;
                color: #1f2937;
                font-size: 16px;
                font-weight: 600;
            }
            
            .ad-description {
                margin: 0;
                color: #6b7280;
                font-size: 14px;
                line-height: 1.4;
            }
            
            .ad-banner-actions {
                text-align: right;
            }
            
            .ad-button {
                display: inline-block;
                background: #3b82f6;
                color: white;
                padding: 8px 16px;
                border-radius: 6px;
                text-decoration: none;
                font-weight: 600;
                font-size: 14px;
                transition: all 0.2s;
                border: none;
            }
            
            .ad-button:hover {
                background: #2563eb;
                transform: translateY(-1px);
                color: white;
                text-decoration: none;
            }
        `;
        
        document.head.appendChild(styles);
    }

    setupAdBannerInteractions(adBanner) {
        const button = adBanner.querySelector('.ad-button');
        if (button) {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const url = button.getAttribute('data-ad-url');
                if (url && url.startsWith('http')) {
                    window.open(url, '_blank');
                }
            });
        }
    }

    setupUI() {
        // Add a toggle button to the OpenWebUI interface
        setTimeout(() => {
            this.addToggleButton();
        }, 2000);
    }

    addToggleButton() {
        // Try multiple locations to add the toggle button
        const possibleLocations = [
            document.querySelector('[data-testid="navbar"]'),
            document.querySelector('.navbar'),
            document.querySelector('[class*="navbar"]'),
            document.querySelector('header'),
            document.querySelector('[class*="header"]'),
            document.querySelector('nav'),
            document.querySelector('[class*="nav"]'),
            document.querySelector('[data-testid="header"]'),
            document.querySelector('[data-testid="top-bar"]'),
            document.querySelector('.top-bar'),
            document.querySelector('[class*="top"]'),
            document.body // Fallback to body if no navbar found
        ];
        
        const targetElement = possibleLocations.find(el => el);
        
        if (targetElement && !document.getElementById('ad-toggle-button')) {
            const toggleButton = document.createElement('button');
            toggleButton.id = 'ad-toggle-button';
            toggleButton.innerHTML = this.enabled ? '🎯 Ads ON' : '🎯 Ads OFF';
            toggleButton.title = this.enabled ? 'Ad Suggestions: ON' : 'Ad Suggestions: OFF';
            toggleButton.className = 'ad-toggle-btn';
            
            // Make it more visible and prominent
            toggleButton.style.cssText = `
                position: ${targetElement === document.body ? 'fixed' : 'relative'};
                top: ${targetElement === document.body ? '20px' : 'auto'};
                right: ${targetElement === document.body ? '20px' : 'auto'};
                background: ${this.enabled ? '#10b981' : '#6b7280'};
                color: white;
                border: none;
                border-radius: 8px;
                padding: 8px 12px;
                cursor: pointer;
                font-size: 13px;
                font-weight: 600;
                margin-left: 8px;
                transition: all 0.2s;
                z-index: 10000;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            `;
            
            toggleButton.addEventListener('click', () => {
                this.enabled = !this.enabled;
                toggleButton.innerHTML = this.enabled ? '🎯 Ads ON' : '🎯 Ads OFF';
                toggleButton.style.background = this.enabled ? '#10b981' : '#6b7280';
                toggleButton.title = this.enabled ? 'Ad Suggestions: ON' : 'Ad Suggestions: OFF';
                
                if (!this.enabled) {
                    // Remove all existing ad banners
                    document.querySelectorAll('.openwebui-ad-banner').forEach(banner => banner.remove());
                } else {
                    // Re-scan for messages when re-enabled
                    this.scanForAssistantMessages();
                }
                
                // Show notification
                this.showNotification(
                    this.enabled ? 'Ad suggestions enabled' : 'Ad suggestions disabled'
                );
            });
            
            targetElement.appendChild(toggleButton);
            
            // Log for debugging
            console.log('🎯 Ad toggle button added to:', targetElement.tagName, targetElement.className);
        } else {
            console.warn('Could not find suitable location for ad toggle button');
        }
    }

    showNotification(message) {
        // Create a simple notification
        const notification = document.createElement('div');
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #10b981;
            color: white;
            padding: 12px 16px;
            border-radius: 6px;
            z-index: 10000;
            font-size: 14px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        `;
        notification.textContent = message;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }
}

// Initialize the extension when the page loads
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        new OpenWebUIAdExtension();
    });
} else {
    new OpenWebUIAdExtension();
}

// Make it globally accessible for manual control
window.OpenWebUIAdExtension = OpenWebUIAdExtension;

// Export for potential use in other scripts
window.OpenWebUIAdExtension = OpenWebUIAdExtension;
