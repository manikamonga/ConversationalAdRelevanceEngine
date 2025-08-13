#!/bin/bash

# OpenWebUI Ad Relevance Integration Startup Script
# This script starts both the integration server and the extension server

echo "🎯 Starting OpenWebUI Ad Relevance Integration..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm first."
    exit 1
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Function to cleanup background processes
cleanup() {
    echo "🛑 Shutting down servers..."
    kill $SERVER_PID $EXTENSION_PID 2>/dev/null
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Start the main integration server
echo "🚀 Starting integration server on port 3001..."
node server.js &
SERVER_PID=$!

# Wait a moment for the server to start
sleep 2

# Start the extension server
echo "🎯 Starting extension server on port 3002..."
node serve-extension.js &
EXTENSION_PID=$!

# Wait a moment for the extension server to start
sleep 2

echo ""
echo "✅ OpenWebUI Ad Relevance Integration is running!"
echo ""
echo "📊 Services:"
echo "   • Integration Server: http://localhost:3001"
echo "   • Extension Server:   http://localhost:3002"
echo "   • Extension File:     http://localhost:3002/openwebui-ad-extension.js"
echo ""
echo "🎯 To integrate with OpenWebUI:"
echo "   1. Open OpenWebUI in your browser"
echo "   2. Open Developer Tools (F12)"
echo "   3. Go to Console tab"
echo "   4. Run: fetch('http://localhost:3002/openwebui-ad-extension.js').then(r=>r.text()).then(code=>eval(code))"
echo ""
echo "🔍 Or create a bookmarklet with this URL:"
echo "   javascript:(function(){var script=document.createElement('script');script.src='http://localhost:3002/openwebui-ad-extension.js';document.head.appendChild(script);})();"
echo ""
echo "🛑 Press Ctrl+C to stop all servers"

# Wait for background processes
wait
