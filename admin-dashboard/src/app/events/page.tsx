'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";

/**
 * Event type definition
 */
interface KafkaEvent {
  id: string;
  topic: string;
  type: string;
  timestamp: string;
  payload: Record<string, unknown>;
}

/**
 * Mock event data for demonstration
 */
const mockEvents: KafkaEvent[] = [
  { id: '1', topic: 'worker-registrations', type: 'worker.registered', timestamp: '2024-04-02T08:30:00Z', payload: { workerId: 'W-123', city: 'Mumbai' } },
  { id: '2', topic: 'policy-purchases', type: 'policy.purchased', timestamp: '2024-04-02T08:28:00Z', payload: { policyId: 'POL-001', planType: 'GOLD' } },
  { id: '3', topic: 'environment-disruptions', type: 'environment.disruption', timestamp: '2024-04-02T08:25:00Z', payload: { city: 'Delhi', triggerType: 'HIGH_AQI', severity: 'HIGH' } },
  { id: '4', topic: 'claims', type: 'claim.initiated', timestamp: '2024-04-02T08:20:00Z', payload: { claimId: 'CLM-001', workerId: 'W-456' } },
  { id: '5', topic: 'claims', type: 'claim.validated', timestamp: '2024-04-02T08:18:00Z', payload: { claimId: 'CLM-001', fraudScore: 0.12 } },
  { id: '6', topic: 'claims', type: 'claim.approved', timestamp: '2024-04-02T08:15:00Z', payload: { claimId: 'CLM-001', amount: 5000 } },
  { id: '7', topic: 'payouts', type: 'payout.completed', timestamp: '2024-04-02T08:10:00Z', payload: { payoutId: 'PAY-001', amount: 5000 } },
];

/**
 * Event type color mapping
 */
function getEventColor(type: string) {
  if (type.includes('registered') || type.includes('purchased')) return 'text-blue-400 bg-blue-500/20';
  if (type.includes('disruption')) return 'text-yellow-400 bg-yellow-500/20';
  if (type.includes('initiated') || type.includes('validated')) return 'text-purple-400 bg-purple-500/20';
  if (type.includes('approved') || type.includes('completed')) return 'text-green-400 bg-green-500/20';
  return 'text-zinc-400 bg-zinc-500/20';
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
 * Events Stream Page
 * Displays real-time Kafka events
 */
export default function EventsPage() {
  const [events, setEvents] = useState<KafkaEvent[]>(mockEvents);
  const [isLive, setIsLive] = useState(true);

  // Simulate real-time events (in production, would use SSE)
  useEffect(() => {
    if (!isLive) return;

    const interval = setInterval(() => {
      const topics = ['worker-registrations', 'policy-purchases', 'claims', 'environment-disruptions'];
      const types = ['worker.registered', 'policy.purchased', 'claim.initiated', 'environment.disruption'];
      const randomIndex = Math.floor(Math.random() * 4);
      
      const newEvent: KafkaEvent = {
        id: `${Date.now()}`,
        topic: topics[randomIndex],
        type: types[randomIndex],
        timestamp: new Date().toISOString(),
        payload: { eventId: Date.now(), simulated: true },
      };
      setEvents(prev => [newEvent, ...prev.slice(0, 49)]);
    }, 5000);

    return () => clearInterval(interval);
  }, [isLive]);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">Event Stream</h1>
          <p className="text-zinc-400 mt-1">
            Real-time Kafka events visualization
          </p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <span className={`w-2 h-2 rounded-full ${isLive ? 'bg-green-500 animate-pulse' : 'bg-zinc-500'}`}></span>
            <span className="text-zinc-400 text-sm">{isLive ? 'Live' : 'Paused'}</span>
          </div>
          <button
            onClick={() => setIsLive(!isLive)}
            className={`px-4 py-2 rounded-lg text-sm font-medium ${
              isLive ? 'bg-red-500/20 text-red-400' : 'bg-green-500/20 text-green-400'
            }`}
          >
            {isLive ? 'Pause' : 'Resume'}
          </button>
        </div>
      </div>

      {/* Event Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Events Today</p>
            <p className="text-2xl font-bold text-white">1,247</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Disruptions</p>
            <p className="text-2xl font-bold text-yellow-400">23</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Claims Events</p>
            <p className="text-2xl font-bold text-purple-400">156</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Payouts</p>
            <p className="text-2xl font-bold text-green-400">89</p>
          </CardContent>
        </Card>
      </div>

      {/* Event List */}
      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Event Timeline</CardTitle>
          <CardDescription className="text-zinc-400">
            Showing last 50 events
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[500px]">
            <div className="space-y-2">
              {events.map((event) => (
                <div
                  key={event.id}
                  className="flex items-start gap-4 p-3 rounded-lg bg-zinc-800/50 hover:bg-zinc-800"
                >
                  <div className="flex-shrink-0">
                    <Badge className={getEventColor(event.type)}>
                      {event.type}
                    </Badge>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-zinc-400 text-sm">
                      Topic: <span className="text-zinc-300 font-mono">{event.topic}</span>
                    </p>
                    <pre className="text-zinc-500 text-xs mt-1 overflow-hidden text-ellipsis">
                      {JSON.stringify(event.payload)}
                    </pre>
                  </div>
                  <div className="flex-shrink-0 text-zinc-500 text-sm font-mono">
                    {formatTime(event.timestamp)}
                  </div>
                </div>
              ))}
            </div>
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  );
}
