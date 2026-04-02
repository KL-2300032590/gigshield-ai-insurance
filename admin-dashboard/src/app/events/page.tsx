'use client';

import { useEffect, useMemo, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { api, KafkaEvent } from '@/lib/api';

function getEventColor(type: string) {
  if (type.includes('registered') || type.includes('purchased')) return 'text-blue-400 bg-blue-500/20';
  if (type.includes('disruption') || type.includes('heavy_rain') || type.includes('high_aqi')) return 'text-yellow-400 bg-yellow-500/20';
  if (type.includes('initiated') || type.includes('validated')) return 'text-purple-400 bg-purple-500/20';
  if (type.includes('approved') || type.includes('completed')) return 'text-green-400 bg-green-500/20';
  return 'text-zinc-400 bg-zinc-500/20';
}

function formatTime(timestamp: string) {
  return new Date(timestamp).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

export default function EventsPage() {
  const [events, setEvents] = useState<KafkaEvent[]>([]);
  const [isLive, setIsLive] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    const loadEvents = async () => {
      try {
        const data = await api.events.getStreamSnapshot();
        if (!mounted) return;
        setEvents(data.slice(0, 50));
        setError(null);
      } catch (err) {
        if (!mounted) return;
        setError((err as Error).message);
      }
    };

    loadEvents();
    if (!isLive) {
      return () => {
        mounted = false;
      };
    }

    const interval = setInterval(loadEvents, 5000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, [isLive]);

  const stats = useMemo(() => {
    const disruptions = events.filter(event => event.type.includes('disruption') || event.type.includes('heavy_rain') || event.type.includes('high_aqi')).length;
    const claimsEvents = events.filter(event => event.type.includes('claim')).length;
    const payouts = events.filter(event => event.type.includes('payout')).length;
    return { total: events.length, disruptions, claimsEvents, payouts };
  }, [events]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">Event Stream</h1>
          <p className="text-zinc-400 mt-1">Live environment and simulation event timeline</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <span className={`w-2 h-2 rounded-full ${isLive ? 'bg-green-500 animate-pulse' : 'bg-zinc-500'}`} />
            <span className="text-zinc-400 text-sm">{isLive ? 'Live' : 'Paused'}</span>
          </div>
          <button
            onClick={() => setIsLive(!isLive)}
            className={`px-4 py-2 rounded-lg text-sm font-medium ${isLive ? 'bg-red-500/20 text-red-400' : 'bg-green-500/20 text-green-400'}`}
          >
            {isLive ? 'Pause' : 'Resume'}
          </button>
        </div>
      </div>

      {error && (
        <Card className="bg-red-500/10 border-red-500/30">
          <CardContent className="pt-4 text-red-300 text-sm">Failed to load events: {error}</CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Recent Events</p><p className="text-2xl font-bold text-white">{stats.total}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Disruptions</p><p className="text-2xl font-bold text-yellow-400">{stats.disruptions}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Claims Events</p><p className="text-2xl font-bold text-purple-400">{stats.claimsEvents}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Payout Events</p><p className="text-2xl font-bold text-green-400">{stats.payouts}</p></CardContent></Card>
      </div>

      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Event Timeline</CardTitle>
          <CardDescription className="text-zinc-400">Showing last 50 events</CardDescription>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[500px]">
            <div className="space-y-2">
              {events.map((event) => (
                <div key={event.id} className="flex items-start gap-4 p-3 rounded-lg bg-zinc-800/50 hover:bg-zinc-800">
                  <div className="flex-shrink-0"><Badge className={getEventColor(event.type)}>{event.type}</Badge></div>
                  <div className="flex-1 min-w-0">
                    <p className="text-zinc-400 text-sm">Topic: <span className="text-zinc-300 font-mono">{event.topic}</span></p>
                    <pre className="text-zinc-500 text-xs mt-1 overflow-hidden text-ellipsis whitespace-pre-wrap">{JSON.stringify(event.payload)}</pre>
                  </div>
                  <div className="flex-shrink-0 text-zinc-500 text-sm font-mono">{formatTime(event.timestamp)}</div>
                </div>
              ))}
            </div>
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  );
}
