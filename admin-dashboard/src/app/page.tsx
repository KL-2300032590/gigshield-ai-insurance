import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

/**
 * Service health card data
 */
const services = [
  { name: 'API Gateway', port: 8080, status: 'UP' as const, icon: '🌐' },
  { name: 'Risk Engine', port: 8081, status: 'UP' as const, icon: '📊' },
  { name: 'Trigger Engine', port: 8082, status: 'UP' as const, icon: '⚡' },
  { name: 'Fraud Detection', port: 8083, status: 'UP' as const, icon: '🔍' },
  { name: 'Claim Service', port: 8084, status: 'UP' as const, icon: '📋' },
  { name: 'Payout Service', port: 8085, status: 'UP' as const, icon: '💰' },
  { name: 'Admin Simulator', port: 8091, status: 'UP' as const, icon: '🎮' },
];

/**
 * Mock statistics data
 */
const stats = {
  activePolicies: 1247,
  pendingClaims: 23,
  approvedClaims: 156,
  totalPayouts: 4520000,
  activeWorkers: 3854,
  todayEvents: 47,
};

/**
 * Status badge color mapping
 */
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

/**
 * Overview Dashboard Page
 * Displays service health status and key statistics
 */
export default function OverviewPage() {
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold text-white">Dashboard Overview</h1>
        <p className="text-zinc-400 mt-1">
          Monitor service health and view key metrics
        </p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2">
            <CardDescription className="text-zinc-400">Active Policies</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-white">{stats.activePolicies.toLocaleString()}</p>
          </CardContent>
        </Card>

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2">
            <CardDescription className="text-zinc-400">Pending Claims</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-yellow-400">{stats.pendingClaims}</p>
          </CardContent>
        </Card>

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2">
            <CardDescription className="text-zinc-400">Approved Claims</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-400">{stats.approvedClaims}</p>
          </CardContent>
        </Card>

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2">
            <CardDescription className="text-zinc-400">Total Payouts</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-blue-400">₹{(stats.totalPayouts / 100000).toFixed(1)}L</p>
          </CardContent>
        </Card>

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2">
            <CardDescription className="text-zinc-400">Active Workers</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-white">{stats.activeWorkers.toLocaleString()}</p>
          </CardContent>
        </Card>

        <Card className="bg-zinc-900 border-zinc-800">
          <CardHeader className="pb-2">
            <CardDescription className="text-zinc-400">Today&apos;s Events</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-purple-400">{stats.todayEvents}</p>
          </CardContent>
        </Card>
      </div>

      {/* Service Health Grid */}
      <div>
        <h2 className="text-xl font-semibold text-white mb-4">Service Health</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {services.map((service) => (
            <Card key={service.name} className="bg-zinc-900 border-zinc-800">
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-2xl">{service.icon}</span>
                    <CardTitle className="text-white text-lg">{service.name}</CardTitle>
                  </div>
                  <Badge className={getStatusColor(service.status)}>
                    {service.status}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-zinc-400">Port</span>
                  <span className="text-zinc-300 font-mono">{service.port}</span>
                </div>
                <div className="flex items-center justify-between text-sm mt-1">
                  <span className="text-zinc-400">Response</span>
                  <span className="text-green-400">&lt;50ms</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* Recent Activity */}
      <div>
        <h2 className="text-xl font-semibold text-white mb-4">Recent Activity</h2>
        <Card className="bg-zinc-900 border-zinc-800">
          <CardContent className="pt-4">
            <div className="space-y-3">
              {[
                { time: '2m ago', event: 'Claim approved', detail: 'CLM-2024-1234 for ₹5,000', type: 'success' },
                { time: '5m ago', event: 'Policy purchased', detail: 'POL-2024-5678 - Silver Plan', type: 'info' },
                { time: '12m ago', event: 'Worker registered', detail: 'Amit Kumar - Mumbai', type: 'info' },
                { time: '15m ago', event: 'Payout completed', detail: '₹8,500 to UPI account', type: 'success' },
                { time: '23m ago', event: 'High AQI detected', detail: 'Delhi - AQI 245', type: 'warning' },
              ].map((activity, i) => (
                <div key={i} className="flex items-center gap-4 py-2 border-b border-zinc-800 last:border-0">
                  <span className={`w-2 h-2 rounded-full ${
                    activity.type === 'success' ? 'bg-green-500' :
                    activity.type === 'warning' ? 'bg-yellow-500' : 'bg-blue-500'
                  }`}></span>
                  <div className="flex-1">
                    <p className="text-white text-sm">{activity.event}</p>
                    <p className="text-zinc-500 text-xs">{activity.detail}</p>
                  </div>
                  <span className="text-zinc-500 text-xs">{activity.time}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
