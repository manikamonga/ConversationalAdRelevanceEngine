#!/bin/bash

# Start the OpenWebUI Ad Relevance Integration

echo "🚀 Starting OpenWebUI Ad Relevance Integration..."

# Check if the ad engine is running
echo "Checking ad engine status..."
if curl -s http://localhost:8080/health > /dev/null 2>&1; then
    echo "✅ Ad engine is running"
else
    echo "⚠️  Ad engine is not running on port 8080"
    echo "   Please start the ad engine first:"
    echo "   cd .. && mvn exec:java -Dexec.mainClass=\"com.adrelevance.demo.ConversationalAdDemo\""
    echo ""
fi

# Start the integration server
echo "Starting integration server..."
npm start
