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

/**
 * Mock policy data
 */
const mockPolicies = [
  { id: 'POL-001', workerId: 'W-123', planType: 'GOLD', city: 'Mumbai', status: 'ACTIVE', premium: 999, coverage: 50000, startDate: '2024-01-15', endDate: '2025-01-15' },
  { id: 'POL-002', workerId: 'W-456', planType: 'SILVER', city: 'Delhi', status: 'ACTIVE', premium: 599, coverage: 30000, startDate: '2024-02-01', endDate: '2025-02-01' },
  { id: 'POL-003', workerId: 'W-789', planType: 'PLATINUM', city: 'Chennai', status: 'ACTIVE', premium: 1499, coverage: 100000, startDate: '2024-03-10', endDate: '2025-03-10' },
  { id: 'POL-004', workerId: 'W-101', planType: 'BRONZE', city: 'Hyderabad', status: 'EXPIRED', premium: 299, coverage: 15000, startDate: '2023-04-01', endDate: '2024-04-01' },
  { id: 'POL-005', workerId: 'W-202', planType: 'GOLD', city: 'Bangalore', status: 'ACTIVE', premium: 999, coverage: 50000, startDate: '2024-01-20', endDate: '2025-01-20' },
];

/**
 * Plan type colors
 */
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

/**
 * Policies Dashboard Page
 */
export default function PoliciesPage() {
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-white">Policies Dashboard</h1>
        <p className="text-zinc-400 mt-1">
          View and manage insurance policies
        </p>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Total Policies</p>
            <p className="text-2xl font-bold text-white">1,247</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Active</p>
            <p className="text-2xl font-bold text-green-400">1,189</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Monthly Revenue</p>
            <p className="text-2xl font-bold text-blue-400">₹8.5L</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Avg Coverage</p>
            <p className="text-2xl font-bold text-white">₹45K</p>
          </CardContent>
        </Card>
      </div>

      {/* Policies Table */}
      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Active Policies</CardTitle>
          <CardDescription className="text-zinc-400">
            Recent policy purchases and renewals
          </CardDescription>
        </CardHeader>
        <CardContent>
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
              {mockPolicies.map((policy) => (
                <TableRow key={policy.id} className="border-zinc-800 hover:bg-zinc-800/50">
                  <TableCell className="font-mono text-white">{policy.id}</TableCell>
                  <TableCell className="text-zinc-300">{policy.workerId}</TableCell>
                  <TableCell>
                    <Badge className={getPlanColor(policy.planType)}>
                      {policy.planType}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-zinc-300">{policy.city}</TableCell>
                  <TableCell className="text-white">₹{policy.premium}/mo</TableCell>
                  <TableCell className="text-white">₹{(policy.coverage / 1000).toFixed(0)}K</TableCell>
                  <TableCell>
                    <Badge className={policy.status === 'ACTIVE' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}>
                      {policy.status}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
