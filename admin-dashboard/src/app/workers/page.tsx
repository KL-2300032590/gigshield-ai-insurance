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
 * Mock worker data
 */
const mockWorkers = [
  { id: 'W-123', name: 'Amit Kumar', email: 'amit@example.com', phone: '9876543210', city: 'Mumbai', gigType: 'DELIVERY', status: 'ACTIVE', registeredAt: '2024-01-15' },
  { id: 'W-456', name: 'Priya Sharma', email: 'priya@example.com', phone: '9876543211', city: 'Delhi', gigType: 'RIDE_SHARE', status: 'ACTIVE', registeredAt: '2024-02-01' },
  { id: 'W-789', name: 'Rajesh Verma', email: 'rajesh@example.com', phone: '9876543212', city: 'Chennai', gigType: 'FOOD_DELIVERY', status: 'INACTIVE', registeredAt: '2024-03-10' },
  { id: 'W-101', name: 'Sneha Patel', email: 'sneha@example.com', phone: '9876543213', city: 'Hyderabad', gigType: 'DELIVERY', status: 'ACTIVE', registeredAt: '2024-04-01' },
  { id: 'W-202', name: 'Vikram Singh', email: 'vikram@example.com', phone: '9876543214', city: 'Bangalore', gigType: 'RIDE_SHARE', status: 'ACTIVE', registeredAt: '2024-01-20' },
];

/**
 * Gig type icon mapping
 */
function getGigIcon(gigType: string) {
  switch (gigType) {
    case 'DELIVERY':
      return '📦';
    case 'RIDE_SHARE':
      return '🚗';
    case 'FOOD_DELIVERY':
      return '🍔';
    default:
      return '👷';
  }
}

/**
 * Workers Management Page
 */
export default function WorkersPage() {
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-white">Workers Management</h1>
        <p className="text-zinc-400 mt-1">
          View and manage registered gig workers
        </p>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Total Workers</p>
            <p className="text-2xl font-bold text-white">3,854</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">Active</p>
            <p className="text-2xl font-bold text-green-400">3,412</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">With Policies</p>
            <p className="text-2xl font-bold text-blue-400">1,247</p>
          </CardContent>
        </Card>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <p className="text-zinc-400 text-sm">New This Month</p>
            <p className="text-2xl font-bold text-purple-400">156</p>
          </CardContent>
        </Card>
      </div>

      {/* Workers Table */}
      <Card className="bg-zinc-900 border-zinc-800">
        <CardHeader>
          <CardTitle className="text-white">Registered Workers</CardTitle>
          <CardDescription className="text-zinc-400">
            Recently registered gig workers
          </CardDescription>
        </CardHeader>
        <CardContent>
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
              {mockWorkers.map((worker) => (
                <TableRow key={worker.id} className="border-zinc-800 hover:bg-zinc-800/50">
                  <TableCell className="font-mono text-white">{worker.id}</TableCell>
                  <TableCell className="text-white">{worker.name}</TableCell>
                  <TableCell className="text-zinc-300">{worker.city}</TableCell>
                  <TableCell className="text-white">
                    <span className="mr-2">{getGigIcon(worker.gigType)}</span>
                    {worker.gigType.replace('_', ' ')}
                  </TableCell>
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
        </CardContent>
      </Card>
    </div>
  );
}
