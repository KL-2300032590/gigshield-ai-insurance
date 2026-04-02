'use client';

import { useEffect, useMemo, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ScrollArea } from "@/components/ui/scroll-area";
import { api, LogEntry } from '@/lib/api';

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

function formatTime(timestamp: string) {
  return new Date(timestamp).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

export default function LogsPage() {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [filter, setFilter] = useState({ level: 'all', service: 'all', search: '' });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    const loadLogs = async () => {
      try {
        const data = await api.logs.getAll();
        if (!mounted) return;
        setLogs(data);
        setError(null);
      } catch (err) {
        if (!mounted) return;
        setError((err as Error).message);
      }
    };

    loadLogs();
    const interval = setInterval(loadLogs, 7000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  const filteredLogs = useMemo(() => logs.filter(log => {
    if (filter.level !== 'all' && log.level !== filter.level) return false;
    if (filter.service !== 'all' && log.service !== filter.service) return false;
    if (filter.search && !log.message.toLowerCase().includes(filter.search.toLowerCase())) return false;
    return true;
  }), [logs, filter]);

  const stats = useMemo(() => ({
    total: logs.length,
    errors: logs.filter(log => log.level === 'ERROR').length,
    warnings: logs.filter(log => log.level === 'WARN').length,
    info: logs.filter(log => log.level === 'INFO').length,
  }), [logs]);

  const services = useMemo(() => Array.from(new Set(logs.map(log => log.service))).sort(), [logs]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">System Logs</h1>
        <p className="text-zinc-400 mt-1">Aggregated operational logs from admin-simulator feeds</p>
      </div>

      {error && (
        <Card className="bg-red-500/10 border-red-500/30">
          <CardContent className="pt-4 text-red-300 text-sm">Failed to load logs: {error}</CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Total Logs</p><p className="text-2xl font-bold text-white">{stats.total}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Errors</p><p className="text-2xl font-bold text-red-400">{stats.errors}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Warnings</p><p className="text-2xl font-bold text-yellow-400">{stats.warnings}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Info</p><p className="text-2xl font-bold text-blue-400">{stats.info}</p></CardContent></Card>
      </div>

      <div className="flex gap-4 flex-wrap">
        <Input
          placeholder="Search logs..."
          value={filter.search}
          onChange={(e) => setFilter(prev => ({ ...prev, search: e.target.value }))}
          className="max-w-xs bg-zinc-900 border-zinc-800 text-white"
        />
        <Select value={filter.level} onValueChange={(v) => setFilter(prev => ({ ...prev, level: v }))}>
          <SelectTrigger className="w-32 bg-zinc-900 border-zinc-800 text-white"><SelectValue placeholder="Level" /></SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Levels</SelectItem>
            <SelectItem value="ERROR" className="text-white">ERROR</SelectItem>
            <SelectItem value="WARN" className="text-white">WARN</SelectItem>
            <SelectItem value="INFO" className="text-white">INFO</SelectItem>
            <SelectItem value="DEBUG" className="text-white">DEBUG</SelectItem>
          </SelectContent>
        </Select>
        <Select value={filter.service} onValueChange={(v) => setFilter(prev => ({ ...prev, service: v }))}>
          <SelectTrigger className="w-44 bg-zinc-900 border-zinc-800 text-white"><SelectValue placeholder="Service" /></SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Services</SelectItem>
            {services.map(service => (
              <SelectItem key={service} value={service} className="text-white">{service}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Log Entries</CardTitle>
          <CardDescription className="text-zinc-400">Showing {filteredLogs.length} entries</CardDescription>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[500px]">
            <div className="space-y-1 font-mono text-sm">
              {filteredLogs.map((log) => (
                <div key={log.id} className="flex items-start gap-3 py-2 px-3 rounded hover:bg-zinc-800/50">
                  <span className="text-zinc-500 flex-shrink-0 w-20">{formatTime(log.timestamp)}</span>
                  <Badge className={`${getLevelColor(log.level)} flex-shrink-0 w-16 justify-center`}>{log.level}</Badge>
                  <span className={`${getServiceColor(log.service)} flex-shrink-0 w-32`}>[{log.service}]</span>
                  <span className="text-zinc-300 flex-1">{log.message}</span>
                </div>
              ))}
            </div>
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  );
}
