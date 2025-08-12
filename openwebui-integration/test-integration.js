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
