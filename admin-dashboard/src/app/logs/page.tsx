'use client';

import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ScrollArea } from "@/components/ui/scroll-area";

/**
 * Mock log data
 */
const mockLogs = [
  { id: '1', level: 'INFO', service: 'api-gateway', message: 'Request received: POST /api/workers/register', timestamp: '2024-04-02T08:30:00Z' },
  { id: '2', level: 'INFO', service: 'risk-engine', message: 'Risk score calculated: worker=W-123, score=0.25', timestamp: '2024-04-02T08:29:55Z' },
  { id: '3', level: 'WARN', service: 'trigger-engine', message: 'High AQI detected in Delhi: value=245', timestamp: '2024-04-02T08:29:50Z' },
  { id: '4', level: 'INFO', service: 'claim-service', message: 'Claim initiated: CLM-2024-0001', timestamp: '2024-04-02T08:29:45Z' },
  { id: '5', level: 'INFO', service: 'fraud-detection', message: 'Fraud check passed: claimId=CLM-2024-0001, score=0.12', timestamp: '2024-04-02T08:29:40Z' },
  { id: '6', level: 'INFO', service: 'claim-service', message: 'Claim approved: CLM-2024-0001, amount=₹5000', timestamp: '2024-04-02T08:29:35Z' },
  { id: '7', level: 'INFO', service: 'payout-service', message: 'Payout processed: payoutId=PAY-001, amount=₹5000', timestamp: '2024-04-02T08:29:30Z' },
  { id: '8', level: 'ERROR', service: 'payout-service', message: 'UPI transfer failed: Invalid VPA format', timestamp: '2024-04-02T08:29:25Z' },
  { id: '9', level: 'DEBUG', service: 'admin-simulator', message: 'Simulation event published: city=Mumbai, type=HEAVY_RAIN', timestamp: '2024-04-02T08:29:20Z' },
  { id: '10', level: 'INFO', service: 'api-gateway', message: 'Health check passed: all services UP', timestamp: '2024-04-02T08:29:15Z' },
];

/**
 * Log level color mapping
 */
function getLevelColor(level: string) {
  switch (level) {
    case 'ERROR':
      return 'bg-red-500/20 text-red-400 border-red-500/30';
    case 'WARN':
      return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
    case 'INFO':
      return 'bg-blue-500/20 text-blue-400 border-blue-500/30';
    case 'DEBUG':
      return 'bg-zinc-500/20 text-zinc-400 border-zinc-500/30';
    default:
      return 'bg-zinc-500/20 text-zinc-400 border-zinc-500/30';
  }
}

/**
 * Service color mapping
 */
function getServiceColor(service: string) {
  const colors: Record<string, string> = {
    'api-gateway': 'text-blue-400',
    'risk-engine': 'text-purple-400',
    'trigger-engine': 'text-yellow-400',
    'fraud-detection': 'text-red-400',
    'claim-service': 'text-green-400',
    'payout-service': 'text-emerald-400',
    'admin-simulator': 'text-orange-400',
  };
  return colors[service] || 'text-zinc-400';
}

/**
 * Format timestamp
 */
function formatTime(timestamp: string) {
  return new Date(timestamp).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

/**
 * System Logs Page
 * Displays aggregated logs from all services
 */
export default function LogsPage() {
  const [filter, setFilter] = useState({ level: 'all', service: 'all', search: '' });

  const filteredLogs = mockLogs.filter(log => {
    if (filter.level !== 'all' && log.level !== filter.level) return false;
    if (filter.service !== 'all' && log.service !== filter.service) return false;
    if (filter.search && !log.message.toLowerCase().includes(filter.search.toLowerCase())) return false;
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-white">System Logs</h1>
        <p className="text-zinc-400 mt-1">
          Aggregated logs from all microservices
        </p>
      </div>

      {/* Log Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Total Logs</p>
            <p className="text-2xl font-bold text-white">24,567</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Errors</p>
            <p className="text-2xl font-bold text-red-400">12</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Warnings</p>
            <p className="text-2xl font-bold text-yellow-400">45</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Info</p>
            <p className="text-2xl font-bold text-blue-400">24,510</p>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="flex gap-4">
        <Input
          placeholder="Search logs..."
          value={filter.search}
          onChange={(e) => setFilter(prev => ({ ...prev, search: e.target.value }))}
          className="max-w-xs bg-zinc-900 border-zinc-800 text-white"
        />
        <Select value={filter.level} onValueChange={(v) => v && setFilter(prev => ({ ...prev, level: v }))}>
          <SelectTrigger className="w-32 bg-zinc-900 border-zinc-800 text-white">
            <SelectValue placeholder="Level" />
          </SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Levels</SelectItem>
            <SelectItem value="ERROR" className="text-white">ERROR</SelectItem>
            <SelectItem value="WARN" className="text-white">WARN</SelectItem>
            <SelectItem value="INFO" className="text-white">INFO</SelectItem>
            <SelectItem value="DEBUG" className="text-white">DEBUG</SelectItem>
          </SelectContent>
        </Select>
        <Select value={filter.service} onValueChange={(v) => v && setFilter(prev => ({ ...prev, service: v }))}>
          <SelectTrigger className="w-40 bg-zinc-900 border-zinc-800 text-white">
            <SelectValue placeholder="Service" />
          </SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Services</SelectItem>
            <SelectItem value="api-gateway" className="text-white">API Gateway</SelectItem>
            <SelectItem value="risk-engine" className="text-white">Risk Engine</SelectItem>
            <SelectItem value="trigger-engine" className="text-white">Trigger Engine</SelectItem>
            <SelectItem value="fraud-detection" className="text-white">Fraud Detection</SelectItem>
            <SelectItem value="claim-service" className="text-white">Claim Service</SelectItem>
            <SelectItem value="payout-service" className="text-white">Payout Service</SelectItem>
            <SelectItem value="admin-simulator" className="text-white">Admin Simulator</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Log List */}
      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Log Entries</CardTitle>
          <CardDescription className="text-zinc-400">
            Showing {filteredLogs.length} entries
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[500px]">
            <div className="space-y-1 font-mono text-sm">
              {filteredLogs.map((log) => (
                <div
                  key={log.id}
                  className="flex items-start gap-3 py-2 px-3 rounded hover:bg-zinc-800/50"
                >
                  <span className="text-zinc-500 flex-shrink-0 w-20">
                    {formatTime(log.timestamp)}
                  </span>
                  <Badge className={`${getLevelColor(log.level)} flex-shrink-0 w-16 justify-center`}>
                    {log.level}
                  </Badge>
                  <span className={`${getServiceColor(log.service)} flex-shrink-0 w-32`}>
                    [{log.service}]
                  </span>
                  <span className="text-zinc-300 flex-1">
                    {log.message}
                  </span>
                </div>
              ))}
            </div>
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  );
}
