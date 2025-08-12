#!/bin/bash

# OpenWebUI Ad Relevance Integration Installer
# This script sets up the integration between OpenWebUI and the Ad Relevance Engine

set -e

echo "🚀 Installing OpenWebUI Ad Relevance Integration..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    print_error "Please run this script from the openwebui-integration directory"
    exit 1
fi

# Check prerequisites
print_status "Checking prerequisites..."

# Check Node.js
if ! command -v node &> /dev/null; then
    print_error "Node.js is not installed. Please install Node.js 16+ first."
    print_status "Visit: https://nodejs.org/"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    print_error "Node.js version 16+ is required. Current version: $(node -v)"
    exit 1
fi

print_success "Node.js $(node -v) is installed"

# Check npm
if ! command -v npm &> /dev/null; then
    print_error "npm is not installed"
    exit 1
fi

print_success "npm $(npm -v) is installed"

# Check Java (for the ad engine)
if ! command -v java &> /dev/null; then
    print_warning "Java is not installed. The ad engine will need to be started separately."
    print_status "Please install Java 11+ to run the ad engine locally."
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 11 ]; then
        print_warning "Java 11+ is recommended. Current version: $(java -version 2>&1 | head -n 1)"
    else
        print_success "Java $(java -version 2>&1 | head -n 1) is installed"
    fi
fi

# Install dependencies
print_status "Installing Node.js dependencies..."
npm install

if [ $? -eq 0 ]; then
    print_success "Dependencies installed successfully"
else
    print_error "Failed to install dependencies"
    exit 1
fi

# Create logs directory
print_status "Creating logs directory..."
mkdir -p logs

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    print_status "Creating .env file..."
    cat > .env << EOF
# OpenWebUI Ad Relevance Integration Environment Variables
NODE_ENV=development
PORT=3001
WS_PORT=3002
AD_ENGINE_HOST=localhost
AD_ENGINE_PORT=8080
OPENWEBUI_HOST=localhost
OPENWEBUI_PORT=3000
EOF
    print_success ".env file created"
fi

# Create startup script
print_status "Creating startup script..."
cat > start-integration.sh << 'EOF'
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
EOF

chmod +x start-integration.sh
print_success "Startup script created: start-integration.sh"

# Create systemd service file (optional)
if command -v systemctl &> /dev/null; then
    print_status "Creating systemd service file..."
    
    SERVICE_FILE="/etc/systemd/system/openwebui-ad-integration.service"
    
    if [ -w "/etc/systemd/system" ]; then
        cat > "$SERVICE_FILE" << EOF
[Unit]
Description=OpenWebUI Ad Relevance Integration
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$(pwd)
ExecStart=$(which node) server.js
Restart=always
RestartSec=10
Environment=NODE_ENV=production

[Install]
WantedBy=multi-user.target
EOF
        print_success "Systemd service file created: $SERVICE_FILE"
        print_status "To enable the service: sudo systemctl enable openwebui-ad-integration"
        print_status "To start the service: sudo systemctl start openwebui-ad-integration"
    else
        print_warning "Cannot write to /etc/systemd/system. Run with sudo to create systemd service."
    fi
fi

# Create Docker Compose file
print_status "Creating Docker Compose configuration..."
cat > docker-compose.yml << EOF
version: '3.8'

services:
  ad-relevance-integration:
    build: .
    ports:
      - "3001:3001"
      - "3002:3002"
    environment:
      - NODE_ENV=production
      - AD_ENGINE_HOST=ad-engine
      - AD_ENGINE_PORT=8080
    depends_on:
      - ad-engine
    restart: unless-stopped

  ad-engine:
    build: ../  # Build from the parent directory (Java project)
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx2g
    restart: unless-stopped

  # Add your OpenWebUI service here if needed
  # openwebui:
  #   image: ghcr.io/open-webui/open-webui:main
  #   ports:
  #     - "3000:8080"
  #   restart: unless-stopped
EOF

print_success "Docker Compose file created: docker-compose.yml"

# Create Dockerfile
print_status "Creating Dockerfile..."
cat > Dockerfile << EOF
FROM node:18-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy application files
COPY . .

# Create logs directory
RUN mkdir -p logs

# Expose ports
EXPOSE 3001 3002

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
  CMD node -e "require('http').get('http://localhost:3001/health', (res) => { process.exit(res.statusCode === 200 ? 0 : 1) })"

# Start the application
CMD ["node", "server.js"]
EOF

print_success "Dockerfile created"

# Create test script
print_status "Creating test script..."
cat > test-integration.js << 'EOF'
const AdRelevanceClient = require('./client.js');

async function testIntegration() {
    console.log('🧪 Testing OpenWebUI Ad Relevance Integration...');
    
    const client = new AdRelevanceClient();
    
    try {
        // Test health check
        console.log('Testing health check...');
        const health = await client.healthCheck();
        if (health) {
            console.log('✅ Health check passed');
        } else {
            console.log('❌ Health check failed');
        }
        
        // Test message processing
        console.log('Testing message processing...');
        const suggestion = await client.processMessage('I need a new smartphone for work');
        if (suggestion) {
            console.log('✅ Ad suggestion received:', suggestion);
        } else {
            console.log('⚠️  No ad suggestion (this might be normal if no relevant ads)');
        }
        
        // Test stats
        console.log('Testing stats endpoint...');
        const stats = await client.getStats();
        if (stats) {
            console.log('✅ Stats received:', stats.analytics);
        } else {
            console.log('❌ Failed to get stats');
        }
        
    } catch (error) {
        console.error('❌ Test failed:', error.message);
    }
}

testIntegration();
EOF

print_success "Test script created: test-integration.js"

# Print installation summary
echo ""
echo "🎉 Installation completed successfully!"
echo ""
echo "📋 Next steps:"
echo ""
echo "1. Start the Ad Relevance Engine (if not already running):"
echo "   cd .. && mvn exec:java -Dexec.mainClass=\"com.adrelevance.demo.ConversationalAdDemo\""
echo ""
echo "2. Start the integration server:"
echo "   ./start-integration.sh"
echo "   # or"
echo "   npm start"
echo ""
echo "3. Test the integration:"
echo "   node test-integration.js"
echo ""
echo "4. Access the integration:"
echo "   - REST API: http://localhost:3001"
echo "   - WebSocket: ws://localhost:3002"
echo "   - Health check: http://localhost:3001/health"
echo ""
echo "5. Integrate with OpenWebUI:"
echo "   - Copy client.js to your OpenWebUI static files"
echo "   - Or use the browser console to load the client"
echo ""
echo "📚 Documentation: README.md"
echo "🐳 Docker: docker-compose up -d"
echo ""

print_success "OpenWebUI Ad Relevance Integration is ready to use!"
