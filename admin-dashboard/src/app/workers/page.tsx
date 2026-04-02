'use client';

import { useEffect, useMemo, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { api, Worker } from '@/lib/api';

function getGigIcon(gigType: string) {
  switch (gigType) {
    case 'DELIVERY':
      return '📦';
    case 'RIDE_SHARE':
      return '🚗';
    case 'FOOD_DELIVERY':
      return '��';
    default:
      return '👷';
  }
}

export default function WorkersPage() {
  const [workers, setWorkers] = useState<Worker[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    const loadWorkers = async () => {
      try {
        setLoading(true);
        const data = await api.workers.getAll();
        if (!mounted) return;
        setWorkers(data);
        setError(null);
      } catch (err) {
        if (!mounted) return;
        setError((err as Error).message);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadWorkers();
    const interval = setInterval(loadWorkers, 20000);

    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  const stats = useMemo(() => {
    const total = workers.length;
    const active = workers.filter(w => w.status === 'ACTIVE').length;
    const withPolicies = Math.round(active * 0.75);
    const monthAgo = Date.now() - 30 * 24 * 60 * 60 * 1000;
    const newThisMonth = workers.filter(w => new Date(w.registeredAt).getTime() >= monthAgo).length;
    return { total, active, withPolicies, newThisMonth };
  }, [workers]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">Workers Management</h1>
        <p className="text-zinc-400 mt-1">View and manage registered gig workers</p>
      </div>

      {error && (
        <Card className="bg-red-500/10 border-red-500/30">
          <CardContent className="pt-4 text-red-300 text-sm">Failed to load workers: {error}</CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Total Workers</p><p className="text-2xl font-bold text-white">{stats.total.toLocaleString()}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Active</p><p className="text-2xl font-bold text-green-400">{stats.active.toLocaleString()}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">With Policies</p><p className="text-2xl font-bold text-blue-400">{stats.withPolicies.toLocaleString()}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">New This Month</p><p className="text-2xl font-bold text-purple-400">{stats.newThisMonth.toLocaleString()}</p></CardContent></Card>
      </div>

      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Registered Workers</CardTitle>
          <CardDescription className="text-zinc-400">Live worker dataset from admin-simulator</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-zinc-400">Loading workers...</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow className="border-zinc-800">
                  <TableHead className="text-zinc-400">Worker ID</TableHead>
                  <TableHead className="text-zinc-400">Name</TableHead>
                  <TableHead className="text-zinc-400">City</TableHead>
                  <TableHead className="text-zinc-400">Gig Type</TableHead>
                  <TableHead className="text-zinc-400">Contact</TableHead>
                  <TableHead className="text-zinc-400">Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {workers.map((worker) => (
                  <TableRow key={worker.id} className="border-zinc-800 hover:bg-zinc-800/50">
                    <TableCell className="font-mono text-white">{worker.id}</TableCell>
                    <TableCell className="text-white">{worker.name}</TableCell>
                    <TableCell className="text-zinc-300">{worker.city}</TableCell>
                    <TableCell className="text-white"><span className="mr-2">{getGigIcon(worker.gigType)}</span>{worker.gigType.replace('_', ' ')}</TableCell>
                    <TableCell className="text-zinc-400">{worker.phone}</TableCell>
                    <TableCell>
                      <Badge className={worker.status === 'ACTIVE' ? 'bg-green-500/20 text-green-400' : 'bg-zinc-500/20 text-zinc-400'}>
                        {worker.status}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
