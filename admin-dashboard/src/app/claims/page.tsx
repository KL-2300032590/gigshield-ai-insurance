'use client';

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

/**
 * Mock claims data for demonstration
 */
const mockClaims = [
  { id: 'CLM-2024-0001', policyId: 'POL-001', workerId: 'W-123', triggerType: 'HEAVY_RAIN', status: 'APPROVED', amount: 5000, city: 'Mumbai', createdAt: '2024-04-02T08:30:00Z' },
  { id: 'CLM-2024-0002', policyId: 'POL-002', workerId: 'W-456', triggerType: 'HIGH_AQI', status: 'PENDING', amount: 3500, city: 'Delhi', createdAt: '2024-04-02T07:15:00Z' },
  { id: 'CLM-2024-0003', policyId: 'POL-003', workerId: 'W-789', triggerType: 'FLOOD', status: 'VALIDATING', amount: 8000, city: 'Chennai', createdAt: '2024-04-01T16:45:00Z' },
  { id: 'CLM-2024-0004', policyId: 'POL-004', workerId: 'W-101', triggerType: 'EXTREME_HEAT', status: 'PAID', amount: 4500, city: 'Hyderabad', createdAt: '2024-04-01T12:00:00Z' },
  { id: 'CLM-2024-0005', policyId: 'POL-005', workerId: 'W-202', triggerType: 'HEAVY_RAIN', status: 'REJECTED', amount: 6000, city: 'Bangalore', createdAt: '2024-04-01T09:30:00Z' },
  { id: 'CLM-2024-0006', policyId: 'POL-006', workerId: 'W-303', triggerType: 'HIGH_AQI', status: 'APPROVED', amount: 2500, city: 'Delhi', createdAt: '2024-03-31T14:20:00Z' },
];

/**
 * Status color mapping
 */
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

/**
 * Trigger type icon mapping
 */
function getTriggerIcon(triggerType: string) {
  switch (triggerType) {
    case 'HEAVY_RAIN':
      return '🌧️';
    case 'FLOOD':
      return '🌊';
    case 'HIGH_AQI':
      return '💨';
    case 'EXTREME_HEAT':
      return '🔥';
    case 'EXTREME_COLD':
      return '❄️';
    default:
      return '⚠️';
  }
}

/**
 * Format date for display
 */
function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/**
 * Claims Management Page
 * Displays and manages insurance claims
 */
export default function ClaimsPage() {
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-white">Claims Management</h1>
        <p className="text-zinc-400 mt-1">
          View and manage insurance claims
        </p>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Total Claims</p>
            <p className="text-2xl font-bold text-white">156</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Pending</p>
            <p className="text-2xl font-bold text-yellow-400">23</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Approved</p>
            <p className="text-2xl font-bold text-green-400">98</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Paid</p>
            <p className="text-2xl font-bold text-emerald-400">89</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Rejected</p>
            <p className="text-2xl font-bold text-red-400">12</p>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="flex gap-4">
        <Input
          placeholder="Search by claim ID..."
          className="max-w-xs bg-zinc-900 border-zinc-800 text-white"
        />
        <Select defaultValue="all">
          <SelectTrigger className="w-40 bg-zinc-900 border-zinc-800 text-white">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Status</SelectItem>
            <SelectItem value="pending" className="text-white">Pending</SelectItem>
            <SelectItem value="approved" className="text-white">Approved</SelectItem>
            <SelectItem value="paid" className="text-white">Paid</SelectItem>
            <SelectItem value="rejected" className="text-white">Rejected</SelectItem>
          </SelectContent>
        </Select>
        <Select defaultValue="all">
          <SelectTrigger className="w-40 bg-zinc-900 border-zinc-800 text-white">
            <SelectValue placeholder="City" />
          </SelectTrigger>
          <SelectContent className="bg-zinc-800 border-zinc-700">
            <SelectItem value="all" className="text-white">All Cities</SelectItem>
            <SelectItem value="mumbai" className="text-white">Mumbai</SelectItem>
            <SelectItem value="delhi" className="text-white">Delhi</SelectItem>
            <SelectItem value="bangalore" className="text-white">Bangalore</SelectItem>
            <SelectItem value="chennai" className="text-white">Chennai</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Claims Table */}
      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Recent Claims</CardTitle>
          <CardDescription className="text-zinc-400">
            Showing most recent claims
          </CardDescription>
        </CardHeader>
        <CardContent>
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
              {mockClaims.map((claim) => (
                <TableRow key={claim.id} className="border-zinc-800 hover:bg-zinc-800/50">
                  <TableCell className="font-mono text-white">{claim.id}</TableCell>
                  <TableCell className="text-white">
                    <span className="mr-2">{getTriggerIcon(claim.triggerType)}</span>
                    {claim.triggerType.replace('_', ' ')}
                  </TableCell>
                  <TableCell className="text-zinc-300">{claim.city}</TableCell>
                  <TableCell className="text-white font-medium">₹{claim.amount.toLocaleString()}</TableCell>
                  <TableCell>
                    <Badge className={getStatusColor(claim.status)}>
                      {claim.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-zinc-400">{formatDate(claim.createdAt)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
