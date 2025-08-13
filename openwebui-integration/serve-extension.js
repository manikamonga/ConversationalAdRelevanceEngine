const express = require('express');
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = 3002;

// Enable CORS for all routes
app.use(cors());

// Serve static files
app.use(express.static(__dirname));

// Serve the extension file with proper headers
app.get('/openwebui-ad-extension.js', (req, res) => {
    res.setHeader('Content-Type', 'application/javascript');
    res.setHeader('Cache-Control', 'no-cache');
    res.sendFile(path.join(__dirname, 'openwebui-ad-extension.js'));
});

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({ 
        status: 'ok', 
        service: 'openwebui-extension-server',
        timestamp: new Date().toISOString()
    });
});

// Root endpoint with instructions
app.get('/', (req, res) => {
    res.send(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>OpenWebUI Ad Extension Server</title>
            <style>
                body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; }
                .code { background: #f4f4f4; padding: 10px; border-radius: 5px; font-family: monospace; }
                .step { margin: 20px 0; padding: 15px; border-left: 4px solid #667eea; background: #f9f9f9; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🎯 OpenWebUI Ad Extension Server</h1>
                <p>Extension server for integrating Conversational Ad Relevance Engine with OpenWebUI</p>
            </div>
            
            <div class="step">
                <h3>✅ Extension Server Running</h3>
                <p>The extension server is running on port ${PORT}</p>
                <p><strong>Extension URL:</strong> <span class="code">http://localhost:${PORT}/openwebui-ad-extension.js</span></p>
            </div>
            
            <div class="step">
                <h3>🚀 Quick Installation</h3>
                <p><strong>Method 1 - Bookmarklet:</strong></p>
                <p>Create a bookmark with this URL:</p>
                <div class="code">javascript:(function(){var script=document.createElement('script');script.src='http://localhost:${PORT}/openwebui-ad-extension.js';document.head.appendChild(script);})();</div>
            </div>
            
            <div class="step">
                <h3>🔧 Manual Installation</h3>
                <p>1. Open OpenWebUI in your browser</p>
                <p>2. Open Developer Tools (F12)</p>
                <p>3. Go to Console tab</p>
                <p>4. Run this command:</p>
                <div class="code">fetch('http://localhost:${PORT}/openwebui-ad-extension.js').then(r=>r.text()).then(code=>eval(code))</div>
            </div>
            
            <div class="step">
                <h3>📋 Requirements</h3>
                <ul>
                    <li>OpenWebUI running on port 3000</li>
                    <li>Ad Relevance Engine running on port 8080</li>
                    <li>Valid OpenAI API key configured</li>
                </ul>
            </div>
            
            <div class="step">
                <h3>🎯 Features</h3>
                <ul>
                    <li>Real-time conversation monitoring</li>
                    <li>Contextual ad suggestions</li>
                    <li>Beautiful sponsored banners</li>
                    <li>Toggle button in OpenWebUI interface</li>
                </ul>
            </div>
            
            <div class="step">
                <h3>🔍 Testing</h3>
                <p>Try sending messages like:</p>
                <ul>
                    <li>"I need a new dress for a party"</li>
                    <li>"Looking for a good restaurant in Milan"</li>
                    <li>"Want to buy a new smartphone"</li>
                </ul>
            </div>
        </body>
        </html>
    `);
});

app.listen(PORT, () => {
    console.log(`🎯 OpenWebUI Extension Server running on http://localhost:${PORT}`);
    console.log(`📁 Extension available at: http://localhost:${PORT}/openwebui-ad-extension.js`);
    console.log(`🔍 Health check: http://localhost:${PORT}/health`);
});
