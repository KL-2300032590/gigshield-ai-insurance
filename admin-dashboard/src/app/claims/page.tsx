'use client';

import { useEffect, useMemo, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { api, Claim } from '@/lib/api';

function getStatusColor(status: string) {
  switch (status) {
    case 'APPROVED':
      return 'bg-green-500/20 text-green-400 border-green-500/30';
    case 'PENDING':
      return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
    case 'VALIDATING':
      return 'bg-blue-500/20 text-blue-400 border-blue-500/30';
    case 'PAID':
      return 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30';
    case 'REJECTED':
      return 'bg-red-500/20 text-red-400 border-red-500/30';
    default:
      return 'bg-zinc-500/20 text-zinc-400 border-zinc-500/30';
  }
}

function getTriggerIcon(triggerType: string) {
  switch (triggerType) {
    case 'HEAVY_RAIN':
      return '🌧️';
    case 'FLOOD':
      return '🌊';
    case 'HIGH_AQI':
    case 'AIR_POLLUTION':
      return '💨';
    case 'EXTREME_HEAT':
      return '🔥';
    case 'EXTREME_COLD':
      return '❄️';
    default:
      return '⚠️';
  }
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function ClaimsPage() {
  const [claims, setClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [cityFilter, setCityFilter] = useState('all');

  useEffect(() => {
    let mounted = true;

    const loadClaims = async () => {
      try {
        setLoading(true);
        const data = await api.claims.getAll({ status: statusFilter, city: cityFilter });
        if (!mounted) return;
        setClaims(data);
        setError(null);
      } catch (err) {
        if (!mounted) return;
        setError((err as Error).message);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadClaims();
    const interval = setInterval(loadClaims, 20000);

    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, [statusFilter, cityFilter]);

  const filteredClaims = useMemo(() => claims.filter((claim) => {
    if (!search) return true;
    return claim.id.toLowerCase().includes(search.toLowerCase());
  }), [claims, search]);

  const stats = useMemo(() => {
    const total = claims.length;
    const pending = claims.filter(claim => claim.status === 'PENDING').length;
    const approved = claims.filter(claim => claim.status === 'APPROVED').length;
    const paid = claims.filter(claim => claim.status === 'PAID').length;
    const rejected = claims.filter(claim => claim.status === 'REJECTED').length;
    return { total, pending, approved, paid, rejected };
  }, [claims]);

  const availableCities = useMemo(() => Array.from(new Set(claims.map(claim => claim.city))).sort(), [claims]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">Claims Management</h1>
        <p className="text-zinc-400 mt-1">View and manage insurance claims</p>
      </div>

      {error && (
        <Card className="bg-red-500/10 border-red-500/30">
          <CardContent className="pt-4 text-red-300 text-sm">Failed to load claims: {error}</CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Total Claims</p><p className="text-2xl font-bold text-white">{stats.total}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Pending</p><p className="text-2xl font-bold text-yellow-400">{stats.pending}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Approved</p><p className="text-2xl font-bold text-green-400">{stats.approved}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Paid</p><p className="text-2xl font-bold text-emerald-400">{stats.paid}</p></CardContent></Card>
        <Card className="bg-zinc-900 border-zinc-800"><CardContent className="pt-4"><p className="text-zinc-400 text-sm">Rejected</p><p className="text-2xl font-bold text-red-400">{stats.rejected}</p></CardContent></Card>
      </div>

      <div className="flex gap-4 flex-wrap">
        <Input
          placeholder="Search by claim ID..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-xs bg-zinc-900 border-zinc-800 text-white"
        />
        <Select value={statusFilter} onValueChange={(v) => v && setStatusFilter(v)}>
          <SelectTrigger className="w-40 bg-zinc-900 border-zinc-800 text-white"><SelectValue placeholder="Status" /></SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Status</SelectItem>
            <SelectItem value="PENDING" className="text-white">Pending</SelectItem>
            <SelectItem value="VALIDATING" className="text-white">Validating</SelectItem>
            <SelectItem value="APPROVED" className="text-white">Approved</SelectItem>
            <SelectItem value="PAID" className="text-white">Paid</SelectItem>
            <SelectItem value="REJECTED" className="text-white">Rejected</SelectItem>
          </SelectContent>
        </Select>
        <Select value={cityFilter} onValueChange={(v) => v && setCityFilter(v)}>
          <SelectTrigger className="w-44 bg-zinc-900 border-zinc-800 text-white"><SelectValue placeholder="City" /></SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Cities</SelectItem>
            {availableCities.map(city => (
              <SelectItem key={city} value={city} className="text-white">{city}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Recent Claims</CardTitle>
          <CardDescription className="text-zinc-400">Showing {filteredClaims.length} claims</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <p className="text-zinc-400">Loading claims...</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow className="border-zinc-800">
                  <TableHead className="text-zinc-400">Claim ID</TableHead>
                  <TableHead className="text-zinc-400">Trigger</TableHead>
                  <TableHead className="text-zinc-400">City</TableHead>
                  <TableHead className="text-zinc-400">Amount</TableHead>
                  <TableHead className="text-zinc-400">Status</TableHead>
                  <TableHead className="text-zinc-400">Created</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredClaims.map((claim) => (
                  <TableRow key={claim.id} className="border-zinc-800 hover:bg-zinc-800/50">
                    <TableCell className="font-mono text-white">{claim.id}</TableCell>
                    <TableCell className="text-white"><span className="mr-2">{getTriggerIcon(claim.triggerType)}</span>{claim.triggerType.replace('_', ' ')}</TableCell>
                    <TableCell className="text-zinc-300">{claim.city}</TableCell>
                    <TableCell className="text-white font-medium">₹{claim.amount.toLocaleString()}</TableCell>
                    <TableCell><Badge className={getStatusColor(claim.status)}>{claim.status}</Badge></TableCell>
                    <TableCell className="text-zinc-400">{formatDate(claim.createdAt)}</TableCell>
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
