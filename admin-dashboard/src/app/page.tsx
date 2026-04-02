'use client';

import { useEffect, useMemo, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { api, ServiceHealth } from '@/lib/api';

function getStatusColor(status: string) {
  switch (status) {
    case 'UP':
      return 'bg-green-500/20 text-green-400 border-green-500/30';
    case 'DOWN':
      return 'bg-red-500/20 text-red-400 border-red-500/30';
    case 'DEGRADED':
      return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
    default:
      return 'bg-zinc-500/20 text-zinc-400 border-zinc-500/30';
  }
}

const serviceIconMap: Record<string, string> = {
  'API Gateway': '🌐',
  'Risk Engine': '📊',
  'Trigger Engine': '⚡',
  'Fraud Detection': '🔍',
  'Claim Service': '📋',
  'Payout Service': '💰',
  'Admin Simulator': '🎮',
};

export default function OverviewPage() {
  const [services, setServices] = useState<ServiceHealth[]>([]);
  const [metrics, setMetrics] = useState<Record<string, number>>({});
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      try {
        const [health, currentMetrics] = await Promise.all([
          api.health.getServicesHealth(),
          api.metrics.get(),
        ]);
        if (!mounted) return;
        setServices(health);
        setMetrics(currentMetrics);
      } catch (err) {
        if (!mounted) return;
        setError((err as Error).message);
      }
    };

    load();
    const interval = setInterval(load, 15000);

    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  const stats = useMemo(() => ({
    activePolicies: Number(metrics.activePolicies ?? 0),
    pendingClaims: Number(metrics.pendingClaims ?? 0),
    approvedClaims: Number(metrics.approvedClaims ?? 0),
    totalPayouts: Number(metrics.totalPayouts ?? 0),
    activeWorkers: Number(metrics.activeWorkers ?? 0),
    todayEvents: Number(metrics.todayEvents ?? 0),
  }), [metrics]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">Dashboard Overview</h1>
        <p className="text-zinc-400 mt-1">
          Monitor service health and live platform metrics
        </p>
      </div>

      {error && (
        <Card className="bg-red-500/10 border-red-500/30">
          <CardContent className="pt-4 text-red-300 text-sm">
            Failed to load dashboard data: {error}
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2"><CardDescription className="text-zinc-400">Active Policies</CardDescription></CardHeader>
          <CardContent><p className="text-2xl font-bold text-white">{stats.activePolicies.toLocaleString()}</p></CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2"><CardDescription className="text-zinc-400">Pending Claims</CardDescription></CardHeader>
          <CardContent><p className="text-2xl font-bold text-yellow-400">{stats.pendingClaims}</p></CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2"><CardDescription className="text-zinc-400">Approved Claims</CardDescription></CardHeader>
          <CardContent><p className="text-2xl font-bold text-green-400">{stats.approvedClaims}</p></CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2"><CardDescription className="text-zinc-400">Total Payouts</CardDescription></CardHeader>
          <CardContent><p className="text-2xl font-bold text-blue-400">₹{(stats.totalPayouts / 100000).toFixed(1)}L</p></CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2"><CardDescription className="text-zinc-400">Active Workers</CardDescription></CardHeader>
          <CardContent><p className="text-2xl font-bold text-white">{stats.activeWorkers.toLocaleString()}</p></CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2"><CardDescription className="text-zinc-400">Today&apos;s Events</CardDescription></CardHeader>
          <CardContent><p className="text-2xl font-bold text-purple-400">{stats.todayEvents}</p></CardContent>
        </Card>
      </div>

      <div>
        <h2 className="text-xl font-semibold text-white mb-4">Service Health</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {services.map((service) => (
            <Card key={`${service.name}-${service.port}`} className="bg-zinc-900 border-zinc-800">
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-2xl">{serviceIconMap[service.name] ?? '🧩'}</span>
                    <CardTitle className="text-white text-lg">{service.name}</CardTitle>
                  </div>
                  <Badge className={getStatusColor(service.status)}>{service.status}</Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-zinc-400">Port</span>
                  <span className="text-zinc-300 font-mono">{service.port}</span>
                </div>
                <div className="flex items-center justify-between text-sm mt-1">
                  <span className="text-zinc-400">Response</span>
                  <span className="text-green-400">{service.responseTime ? `${service.responseTime}ms` : 'n/a'}</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      <div>
        <h2 className="text-xl font-semibold text-white mb-4">Recent Activity</h2>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <div className="space-y-3">
              {[
                { event: 'Health sync complete', detail: `${services.length} services monitored`, type: 'success' },
                { event: 'Claims processing', detail: `${stats.pendingClaims} claims pending validation`, type: 'info' },
                { event: 'Policy portfolio', detail: `${stats.activePolicies} active policies running`, type: 'info' },
                { event: 'Payout exposure', detail: `₹${stats.totalPayouts.toLocaleString()} approved/paid`, type: 'success' },
                { event: 'Event stream', detail: `${stats.todayEvents} environment events in last 24h`, type: 'warning' },
              ].map((activity, i) => (
                <div key={i} className="flex items-center gap-4 py-2 border-b border-zinc-800 last:border-0">
                  <span className={`w-2 h-2 rounded-full ${activity.type === 'success' ? 'bg-green-500' : activity.type === 'warning' ? 'bg-yellow-500' : 'bg-blue-500'}`} />
                  <div className="flex-1">
                    <p className="text-white text-sm">{activity.event}</p>
                    <p className="text-zinc-500 text-xs">{activity.detail}</p>
                  </div>
                  <span className="text-zinc-500 text-xs">live</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
