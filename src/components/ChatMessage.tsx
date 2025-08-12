import React from 'react';
import { Message, AdSuggestion } from '../types';
import { Clock, Star, Tag, TrendingUp } from 'lucide-react';

interface ChatMessageProps {
  message: Message;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ message }) => {
  const formatTime = (date: Date) => {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const getRelevanceColor = (score: number) => {
    if (score >= 0.8) return 'text-success-500';
    if (score >= 0.6) return 'text-warning-500';
    return 'text-gray-500';
  };

  const renderAdSuggestion = (adSuggestion: AdSuggestion) => (
    <div className="mt-3 p-4 bg-gradient-to-r from-secondary-50 to-secondary-100 rounded-xl border border-secondary-200">
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center space-x-2">
          <Tag className="w-4 h-4 text-secondary-600" />
          <span className="text-sm font-medium text-secondary-800">
            {adSuggestion.ad.brandName}
          </span>
        </div>
        <div className="flex items-center space-x-1">
          <Star className={`w-4 h-4 ${getRelevanceColor(adSuggestion.relevanceScore)}`} />
          <span className={`text-sm font-medium ${getRelevanceColor(adSuggestion.relevanceScore)}`}>
            {(adSuggestion.relevanceScore * 100).toFixed(0)}%
          </span>
        </div>
      </div>
      
      <h4 className="font-semibold text-gray-900 mb-1">{adSuggestion.ad.title}</h4>
      <p className="text-sm text-gray-600 mb-3">{adSuggestion.ad.description}</p>
      
      <div className="flex items-center justify-between">
        <div className="flex flex-wrap gap-1">
          {adSuggestion.ad.categories.slice(0, 3).map((category, index) => (
            <span
              key={index}
              className="px-2 py-1 bg-secondary-100 text-secondary-700 text-xs rounded-full"
            >
              {category}
            </span>
          ))}
        </div>
        <button className="px-4 py-2 bg-secondary-500 hover:bg-secondary-600 text-white text-sm rounded-lg transition-colors">
          {adSuggestion.ad.callToAction}
        </button>
      </div>
    </div>
  );

  return (
    <div className={`chat-message flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'} mb-4`}>
      <div className={`message-bubble ${message.sender === 'user' ? 'user-message' : 'bot-message'}`}>
        <div className="flex items-start space-x-2">
          {message.sender === 'bot' && (
            <div className="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center flex-shrink-0">
              <span className="text-white text-sm font-medium">AI</span>
            </div>
          )}
          
          <div className="flex-1">
            <div className="flex items-center justify-between mb-1">
              <span className="text-sm font-medium">
                {message.sender === 'user' ? 'You' : 'AI Assistant'}
              </span>
              <div className="flex items-center space-x-1 text-xs text-gray-500">
                <Clock className="w-3 h-3" />
                <span>{formatTime(message.timestamp)}</span>
              </div>
            </div>
            
            <div className="text-sm leading-relaxed">
              {message.content}
            </div>
            
            {message.adSuggestion && renderAdSuggestion(message.adSuggestion)}
            
            {message.type === 'ad-suggestion' && (
              <div className="mt-2 flex items-center space-x-2 text-xs text-gray-500">
                <TrendingUp className="w-3 h-3" />
                <span>Context-aware ad suggestion</span>
              </div>
            )}
          </div>
          
          {message.sender === 'user' && (
            <div className="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center flex-shrink-0">
              <span className="text-white text-sm font-medium">U</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ChatMessage;
