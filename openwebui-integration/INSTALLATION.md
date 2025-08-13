# OpenWebUI Ad Relevance Integration

This guide shows you how to integrate the Conversational Ad Relevance Engine with OpenWebUI to display contextual sponsored banners during conversations.

## 🚀 Quick Setup

### Prerequisites
- OpenWebUI installed and running
- Conversational Ad Relevance Engine running on port 8080
- Browser with developer tools access

### Method 1: Browser Extension (Recommended)

1. **Install a User Script Manager**
   - Install [Tampermonkey](https://www.tampermonkey.net/) or [Greasemonkey](https://addons.mozilla.org/en-US/firefox/addon/greasemonkey/)

2. **Create a New Script**
   - Open your user script manager
   - Create a new script
   - Copy the contents of `openwebui-ad-extension.js` into the script editor

3. **Configure the Script**
   - Set the script to run on: `http://localhost:3000/*` (or your OpenWebUI URL)
   - Save the script

4. **Start Your Services**
   ```bash
   # Start the Ad Relevance Engine
   cd ConversationalAdRelevanceEngine
   mvn exec:java -Dexec.mainClass="com.adrelevance.api.ChatGPTAdRelevanceAPI"
   
   # Start OpenWebUI (if not already running)
   cd ../open-webui
   # Follow OpenWebUI installation instructions
   ```

### Method 2: Direct Browser Injection

1. **Open OpenWebUI in your browser**

2. **Open Developer Tools** (F12)

3. **Go to Console tab**

4. **Paste and run the extension code:**
   ```javascript
   // Copy the entire content of openwebui-ad-extension.js here
   ```

### Method 3: Bookmarklet

1. **Create a bookmark** in your browser

2. **Set the URL to:**
   ```javascript
   javascript:(function(){
     var script = document.createElement('script');
     script.src = 'http://localhost:3001/openwebui-ad-extension.js';
     document.head.appendChild(script);
   })();
   ```

3. **Click the bookmark** when on OpenWebUI to load the extension

## 🎯 Features

### Automatic Ad Detection
- Monitors conversations in real-time
- Analyzes context using ChatGPT
- Displays relevant sponsored banners

### Beautiful Ad Banners
- Gradient background design
- Professional "Sponsored" labeling
- Clickable call-to-action buttons
- Hover effects and animations

### User Control
- Toggle button in the interface (🎯)
- Enable/disable ad suggestions
- Visual feedback and notifications

## 🔧 Configuration

### Customize Ad Engine URL
Edit the extension to change the ad engine endpoint:
```javascript
this.adEngineUrl = 'http://localhost:8080/api/chatgpt/process-message';
```

### Adjust Relevance Threshold
```javascript
this.minRelevanceScore = 0.3; // Lower = more ads, Higher = fewer ads
```

### Customize User ID
```javascript
this.userId = 'openwebui_user'; // Change to track different users
```

## 🎨 Customization

### Modify Ad Banner Styling
The extension includes CSS styles that can be customized:
- Colors and gradients
- Spacing and layout
- Animations and effects
- Typography

### Add Custom Ad Logic
Extend the `processAssistantMessage` method to add custom filtering or processing logic.

## 🐛 Troubleshooting

### Extension Not Loading
1. Check browser console for errors
2. Verify OpenWebUI is running
3. Ensure the script is enabled in your user script manager

### No Ads Appearing
1. Verify the Ad Relevance Engine is running on port 8080
2. Check browser console for API errors
3. Ensure you have a valid OpenAI API key configured
4. Try sending messages that mention shopping, travel, or products

### Ads Not Relevant
1. Adjust the `minRelevanceScore` threshold
2. Check the ChatGPT API responses in the browser console
3. Verify the conversation context is being captured correctly

## 🔍 Debugging

### Enable Debug Logging
Add this to the extension constructor:
```javascript
this.debug = true;
```

### Check API Responses
Monitor the browser's Network tab to see API calls to the ad engine.

### View Extension Status
The extension logs initialization and errors to the browser console.

## 📱 Browser Compatibility

- ✅ Chrome/Chromium
- ✅ Firefox
- ✅ Safari
- ✅ Edge

## 🔒 Security Notes

- The extension only communicates with your local ad engine
- No data is sent to external services (except OpenAI API)
- All processing happens locally
- Ad URLs are opened in new tabs for security

## 🚀 Advanced Usage

### Multiple OpenWebUI Instances
The extension automatically handles multiple chat sessions and conversations.

### Custom Ad Providers
Modify the `getAdSuggestion` method to integrate with different ad providers.

### Analytics Integration
Add analytics tracking to monitor ad performance and user interactions.

## 📞 Support

If you encounter issues:
1. Check the browser console for error messages
2. Verify all services are running correctly
3. Test with the demo interface first
4. Review the troubleshooting section above

## 🎉 Success Indicators

When working correctly, you should see:
- 🚀 Extension initialization message in console
- 🎯 Toggle button in the OpenWebUI interface
- 💡 Sponsored banners appearing after assistant messages
- Smooth animations and hover effects
- Clickable ad buttons that open in new tabs
