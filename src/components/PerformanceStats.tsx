import React from 'react';
import { EngineStats, PerformanceMetrics } from '../types';
import { 
  Activity, 
  Users, 
  MessageSquare, 
  ShoppingBag, 
  Zap, 
  Clock, 
  TrendingUp, 
  Database 
} from 'lucide-react';

interface PerformanceStatsProps {
  stats: EngineStats;
  performance: PerformanceMetrics;
}

const PerformanceStats: React.FC<PerformanceStatsProps> = ({ stats, performance }) => {
  const getLatencyColor = (latency: number) => {
    if (latency < 50) return 'performance-excellent';
    if (latency < 100) return 'performance-good';
    return 'bg-red-100 text-red-800';
  };

  const getThroughputColor = (throughput: number) => {
    if (throughput > 500) return 'performance-excellent';
    if (throughput > 100) return 'performance-good';
    return 'bg-red-100 text-red-800';
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      {/* Engine Stats */}
      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-primary-500/20 rounded-lg">
            <MessageSquare className="w-5 h-5 text-primary-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Active Conversations</p>
            <p className="text-xl font-semibold text-white">{stats.activeConversations}</p>
          </div>
        </div>
      </div>

      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-secondary-500/20 rounded-lg">
            <ShoppingBag className="w-5 h-5 text-secondary-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Ad Inventory</p>
            <p className="text-xl font-semibold text-white">{stats.adInventorySize}</p>
          </div>
        </div>
      </div>

      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-success-500/20 rounded-lg">
            <Users className="w-5 h-5 text-success-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Total Users</p>
            <p className="text-xl font-semibold text-white">{stats.totalUsers}</p>
          </div>
        </div>
      </div>

      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-warning-500/20 rounded-lg">
            <Activity className="w-5 h-5 text-warning-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Avg Latency</p>
            <p className="text-xl font-semibold text-white">{stats.averageLatency}ms</p>
          </div>
        </div>
      </div>

      {/* Performance Metrics */}
      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-green-500/20 rounded-lg">
            <Zap className="w-5 h-5 text-green-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Current Latency</p>
            <span className={`performance-badge ${getLatencyColor(performance.latency)}`}>
              {performance.latency}ms
            </span>
          </div>
        </div>
      </div>

      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-blue-500/20 rounded-lg">
            <TrendingUp className="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Throughput</p>
            <span className={`performance-badge ${getThroughputColor(performance.throughput)}`}>
              {performance.throughput}/sec
            </span>
          </div>
        </div>
      </div>

      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-purple-500/20 rounded-lg">
            <Clock className="w-5 h-5 text-purple-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Cache Hit Rate</p>
            <span className="performance-badge bg-purple-100 text-purple-800">
              {(performance.cacheHitRate * 100).toFixed(1)}%
            </span>
          </div>
        </div>
      </div>

      <div className="stats-card">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-indigo-500/20 rounded-lg">
            <Database className="w-5 h-5 text-indigo-600" />
          </div>
          <div>
            <p className="text-sm text-white/70">Memory Usage</p>
            <span className="performance-badge bg-indigo-100 text-indigo-800">
              {performance.memoryUsage}MB
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PerformanceStats;
