import React, { useState } from 'react';
import { UserState } from '../types';
import { Settings, Plus, X, Heart, Shield } from 'lucide-react';

interface UserPreferencesProps {
  userPreferences: UserState;
  onUpdatePreferences: (preferences: Partial<UserState>) => void;
}

const UserPreferences: React.FC<UserPreferencesProps> = ({ 
  userPreferences, 
  onUpdatePreferences 
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [newInterest, setNewInterest] = useState('');
  const [newBlockedCategory, setNewBlockedCategory] = useState('');

  const commonInterests = [
    'technology', 'fashion', 'travel', 'food', 'sports', 'music', 
    'movies', 'books', 'fitness', 'beauty', 'gaming', 'education'
  ];

  const addInterest = () => {
    if (newInterest.trim() && !userPreferences.interests.includes(newInterest.trim())) {
      onUpdatePreferences({
        interests: [...userPreferences.interests, newInterest.trim()]
      });
      setNewInterest('');
    }
  };

  const removeInterest = (interest: string) => {
    onUpdatePreferences({
      interests: userPreferences.interests.filter(i => i !== interest)
    });
  };

  const addBlockedCategory = () => {
    if (newBlockedCategory.trim() && !userPreferences.blockedCategories.includes(newBlockedCategory.trim())) {
      onUpdatePreferences({
        blockedCategories: [...userPreferences.blockedCategories, newBlockedCategory.trim()]
      });
      setNewBlockedCategory('');
    }
  };

  const removeBlockedCategory = (category: string) => {
    onUpdatePreferences({
      blockedCategories: userPreferences.blockedCategories.filter(c => c !== category)
    });
  };

  const toggleAdPreferences = () => {
    onUpdatePreferences({
      adPreferencesEnabled: !userPreferences.adPreferencesEnabled
    });
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 px-4 py-2 bg-white/10 backdrop-blur-sm rounded-lg border border-white/20 text-white hover:bg-white/20 transition-colors"
      >
        <Settings className="w-4 h-4" />
        <span>Preferences</span>
      </button>

      {isOpen && (
        <div className="absolute top-full right-0 mt-2 w-80 bg-white rounded-xl shadow-2xl border border-gray-200 p-6 z-50 animate-slide-up">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">User Preferences</h3>
            <button
              onClick={() => setIsOpen(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Ad Preferences Toggle */}
          <div className="mb-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Heart className="w-4 h-4 text-primary-500" />
                <span className="text-sm font-medium text-gray-700">Ad Suggestions</span>
              </div>
              <button
                onClick={toggleAdPreferences}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                  userPreferences.adPreferencesEnabled ? 'bg-primary-500' : 'bg-gray-300'
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                    userPreferences.adPreferencesEnabled ? 'translate-x-6' : 'translate-x-1'
                  }`}
                />
              </button>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              {userPreferences.adPreferencesEnabled 
                ? 'Personalized ads are enabled' 
                : 'Personalized ads are disabled'
              }
            </p>
          </div>

          {/* Interests */}
          <div className="mb-6">
            <div className="flex items-center space-x-2 mb-3">
              <Heart className="w-4 h-4 text-primary-500" />
              <h4 className="text-sm font-medium text-gray-700">Interests</h4>
            </div>
            
            <div className="flex flex-wrap gap-2 mb-3">
              {userPreferences.interests.map((interest, index) => (
                <span
                  key={index}
                  className="flex items-center space-x-1 px-3 py-1 bg-primary-100 text-primary-700 text-xs rounded-full"
                >
                  <span>{interest}</span>
                  <button
                    onClick={() => removeInterest(interest)}
                    className="hover:text-primary-900"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </span>
              ))}
            </div>

            <div className="flex space-x-2">
              <input
                type="text"
                value={newInterest}
                onChange={(e) => setNewInterest(e.target.value)}
                placeholder="Add interest..."
                className="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:border-primary-500"
                onKeyPress={(e) => e.key === 'Enter' && addInterest()}
              />
              <button
                onClick={addInterest}
                className="px-3 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors"
              >
                <Plus className="w-4 h-4" />
              </button>
            </div>

            <div className="mt-2">
              <p className="text-xs text-gray-500 mb-2">Quick add:</p>
              <div className="flex flex-wrap gap-1">
                {commonInterests
                  .filter(interest => !userPreferences.interests.includes(interest))
                  .slice(0, 6)
                  .map((interest, index) => (
                    <button
                      key={index}
                      onClick={() => {
                        onUpdatePreferences({
                          interests: [...userPreferences.interests, interest]
                        });
                      }}
                      className="px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded hover:bg-gray-200 transition-colors"
                    >
                      {interest}
                    </button>
                  ))}
              </div>
            </div>
          </div>

          {/* Blocked Categories */}
          <div>
            <div className="flex items-center space-x-2 mb-3">
              <Shield className="w-4 h-4 text-red-500" />
              <h4 className="text-sm font-medium text-gray-700">Blocked Categories</h4>
            </div>
            
            <div className="flex flex-wrap gap-2 mb-3">
              {userPreferences.blockedCategories.map((category, index) => (
                <span
                  key={index}
                  className="flex items-center space-x-1 px-3 py-1 bg-red-100 text-red-700 text-xs rounded-full"
                >
                  <span>{category}</span>
                  <button
                    onClick={() => removeBlockedCategory(category)}
                    className="hover:text-red-900"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </span>
              ))}
            </div>

            <div className="flex space-x-2">
              <input
                type="text"
                value={newBlockedCategory}
                onChange={(e) => setNewBlockedCategory(e.target.value)}
                placeholder="Block category..."
                className="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:border-red-500"
                onKeyPress={(e) => e.key === 'Enter' && addBlockedCategory()}
              />
              <button
                onClick={addBlockedCategory}
                className="px-3 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
              >
                <Plus className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserPreferences;
