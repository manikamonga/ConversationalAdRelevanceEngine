import React from 'react';
import { Bot } from 'lucide-react';

const TypingIndicator: React.FC = () => {
  return (
    <div className="flex items-start space-x-2 mb-4 animate-fade-in">
      <div className="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center flex-shrink-0">
        <Bot className="w-4 h-4 text-white" />
      </div>
      
      <div className="bg-white rounded-2xl px-4 py-3 shadow-lg">
        <div className="flex items-center space-x-1">
          <span className="text-sm text-gray-600 mr-2">AI is thinking</span>
          <div className="flex space-x-1">
            <div className="typing-dot animate-pulse" style={{ animationDelay: '0ms' }}></div>
            <div className="typing-dot animate-pulse" style={{ animationDelay: '150ms' }}></div>
            <div className="typing-dot animate-pulse" style={{ animationDelay: '300ms' }}></div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TypingIndicator;
