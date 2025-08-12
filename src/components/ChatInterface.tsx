import React, { useState, useEffect, useRef } from 'react';
import { Message, UserState } from '../types';
import { apiService } from '../services/api';
import ChatMessage from './ChatMessage';
import TypingIndicator from './TypingIndicator';
import UserPreferences from './UserPreferences';
import { Send, RefreshCw, Zap } from 'lucide-react';

const ChatInterface: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [userPreferences, setUserPreferences] = useState<UserState>({
    userId: 'user_' + Math.random().toString(36).substr(2, 9),
    interests: ['technology', 'fashion', 'travel'],
    topicPreferences: {},
    currentMood: 'neutral',
    adPreferencesEnabled: true,
    blockedCategories: ['gambling']
  });

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async () => {
    if (!inputMessage.trim() || isLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputMessage.trim(),
      sender: 'user',
      timestamp: new Date(),
      type: 'message'
    };

    setMessages(prev => [...prev, userMessage]);
    setInputMessage('');
    setIsLoading(true);

    try {
      const adSuggestion = await apiService.processMessage(inputMessage.trim());
      
      if (adSuggestion && userPreferences.adPreferencesEnabled) {
        const botMessage: Message = {
          id: (Date.now() + 1).toString(),
          content: adSuggestion.response,
          sender: 'bot',
          timestamp: new Date(),
          type: 'ad-suggestion',
          adSuggestion: adSuggestion
        };
        setMessages(prev => [...prev, botMessage]);
      } else {
        // Fallback response when no ad is relevant or ads are disabled
        const fallbackResponses = [
          "That's interesting! Tell me more about that.",
          "I see what you mean. What are your thoughts on that?",
          "Thanks for sharing! Is there anything specific you'd like to know?",
          "I understand. How can I help you with that?"
        ];
        
        const botMessage: Message = {
          id: (Date.now() + 1).toString(),
          content: fallbackResponses[Math.floor(Math.random() * fallbackResponses.length)],
          sender: 'bot',
          timestamp: new Date(),
          type: 'message'
        };
        setMessages(prev => [...prev, botMessage]);
      }
    } catch (error) {
      console.error('Failed to process message:', error);
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: "I'm sorry, I'm having trouble processing your message right now. Please try again.",
        sender: 'bot',
        timestamp: new Date(),
        type: 'message'
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const handleUpdatePreferences = async (preferences: Partial<UserState>) => {
    const updatedPreferences = { ...userPreferences, ...preferences };
    setUserPreferences(updatedPreferences);
    
    try {
      await apiService.updateUserPreferences(preferences);
    } catch (error) {
      console.error('Failed to update preferences:', error);
    }
  };

  const clearChat = () => {
    setMessages([]);
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  const suggestedMessages = [
    "I need a new smartphone",
    "I'm looking for summer clothes",
    "I want to plan a vacation",
    "I'm interested in fitness",
    "I love trying new foods"
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-purple-50">
      {/* Header */}
      <div className="glass border-b border-white/20">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-gradient-to-r from-primary-500 to-secondary-500 rounded-lg flex items-center justify-center">
                <Zap className="w-4 h-4 text-white" />
              </div>
              <div>
                <h1 className="text-lg font-semibold gradient-text">
                  Ad Relevance Chat
                </h1>
              </div>
            </div>
            
            <div className="flex items-center space-x-2">
              <UserPreferences 
                userPreferences={userPreferences}
                onUpdatePreferences={handleUpdatePreferences}
              />
              <button
                onClick={clearChat}
                className="flex items-center space-x-1 px-3 py-2 bg-white/10 backdrop-blur-sm rounded-lg border border-white/20 text-white hover:bg-white/20 transition-colors text-sm"
              >
                <RefreshCw className="w-3 h-3" />
                <span>Clear</span>
              </button>
            </div>
          </div>
        </div>
      </div>



      {/* Chat Container */}
      <div className="max-w-4xl mx-auto px-4 py-6">
        <div className="bg-white rounded-2xl shadow-xl border border-gray-200 overflow-hidden">
          {/* Messages Area */}
          <div className="h-[500px] overflow-y-auto p-6">
            {messages.length === 0 ? (
              <div className="text-center py-12">
                <div className="w-16 h-16 bg-gradient-to-r from-primary-500 to-secondary-500 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Zap className="w-8 h-8 text-white" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  Welcome to the Conversational Ad Relevance Engine
                </h3>
                <p className="text-gray-600 mb-6">
                  Start a conversation and see how context-aware advertising works in real-time!
                </p>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-w-md mx-auto">
                  {suggestedMessages.map((message, index) => (
                    <button
                      key={index}
                      onClick={() => setInputMessage(message)}
                      className="px-4 py-2 text-sm bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors"
                    >
                      {message}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <>
                {messages.map((message) => (
                  <ChatMessage key={message.id} message={message} />
                ))}
                {isLoading && <TypingIndicator />}
              </>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Area */}
          <div className="border-t border-gray-200 p-4">
            <div className="flex space-x-3">
              <input
                ref={inputRef}
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Type your message..."
                className="input-field flex-1"
                disabled={isLoading}
              />
              <button
                onClick={handleSendMessage}
                disabled={!inputMessage.trim() || isLoading}
                className="send-button disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Send className="w-4 h-4" />
                <span>Send</span>
              </button>
            </div>
            
            {userPreferences.interests.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                <span className="text-xs text-gray-500">Your interests:</span>
                {userPreferences.interests.map((interest, index) => (
                  <span
                    key={index}
                    className="px-2 py-1 bg-primary-100 text-primary-700 text-xs rounded-full"
                  >
                    {interest}
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatInterface;
