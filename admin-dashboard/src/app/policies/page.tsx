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
import { api, Policy } from '@/lib/api';

function getPlanColor(plan: string) {
  switch (plan) {
    case 'PLATINUM':
      return 'bg-purple-500/20 text-purple-400 border-purple-500/30';
    case 'GOLD':
      return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
    case 'SILVER':
      return 'bg-zinc-400/20 text-zinc-300 border-zinc-400/30';
    case 'BRONZE':
      return 'bg-orange-500/20 text-orange-400 border-orange-500/30';
    default:
      return 'bg-zinc-500/20 text-zinc-400 border-zinc-500/30';
  }
}

export default function PoliciesPage() {
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    const loadPolicies = async () => {
      try {
        setLoading(true);
        const data = await api.policies.getAll();
        if (!mounted) return;
        setPolicies(data);
        setError(null);
      } catch (err) {
        if (!mounted) return;
        setError((err as Error).message);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadPolicies();
    const interval = setInterval(loadPolicies, 20000);

    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  const stats = useMemo(() => {
    const totalPolicies = policies.length;
    const activePolicies = policies.filter(p => p.status === 'ACTIVE').length;
    const monthlyRevenue = policies.reduce((sum, policy) => sum + policy.premium, 0);
    const avgCoverage = totalPolicies > 0 ? Math.round(policies.reduce((sum, policy) => sum + policy.coverage, 0) / totalPolicies) : 0;
    return { totalPolicies, activePolicies, monthlyRevenue, avgCoverage };
  }, [policies]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">Policies Dashboard</h1>
        <p className="text-zinc-400 mt-1">View and manage insurance policies</p>
      </div>

      {error && (
        <Card className="bg-red-500/10 border-red-500/30">
          <CardContent className="pt-4 text-red-300 text-sm">Failed to load policies: {error}</CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Total Policies</p><p className="text-2xl font-bold text-white">{stats.totalPolicies.toLocaleString()}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Active</p><p className="text-2xl font-bold text-green-400">{stats.activePolicies.toLocaleString()}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Monthly Revenue</p><p className="text-2xl font-bold text-blue-400">₹{(stats.monthlyRevenue / 100000).toFixed(2)}L</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Avg Coverage</p><p className="text-2xl font-bold text-white">₹{Math.round(stats.avgCoverage / 1000)}K</p></CardContent></Card>
      </div>

      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Active Policies</CardTitle>
          <CardDescription className="text-zinc-400">Live policy portfolio from admin-simulator</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-zinc-400">Loading policies...</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow className="border-zinc-800">
                  <TableHead className="text-zinc-400">Policy ID</TableHead>
                  <TableHead className="text-zinc-400">Worker</TableHead>
                  <TableHead className="text-zinc-400">Plan</TableHead>
                  <TableHead className="text-zinc-400">City</TableHead>
                  <TableHead className="text-zinc-400">Premium</TableHead>
                  <TableHead className="text-zinc-400">Coverage</TableHead>
                  <TableHead className="text-zinc-400">Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {policies.map((policy) => (
                  <TableRow key={policy.id} className="border-zinc-800 hover:bg-zinc-800/50">
                    <TableCell className="font-mono text-white">{policy.id}</TableCell>
                    <TableCell className="text-zinc-300">{policy.workerId}</TableCell>
                    <TableCell><Badge className={getPlanColor(policy.planType)}>{policy.planType}</Badge></TableCell>
                    <TableCell className="text-zinc-300">{policy.city}</TableCell>
                    <TableCell className="text-white">₹{policy.premium}/mo</TableCell>
                    <TableCell className="text-white">₹{Math.round(policy.coverage / 1000)}K</TableCell>
                    <TableCell>
                      <Badge className={policy.status === 'ACTIVE' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}>
                        {policy.status}
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
