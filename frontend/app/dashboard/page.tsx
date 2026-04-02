'use client'

import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useRouter } from 'next/navigation'
import { Shield, FileText, AlertCircle, Wallet, ChevronRight, TrendingUp, CloudRain } from 'lucide-react'
import { DashboardLayout } from '@/components/layout/DashboardLayout'
import { Card, CardBody } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { StatusBadge } from '@/components/ui/Badge'
import { useAuthStore, useDashboardStore } from '@/lib/store'
import { policyApi, claimsApi, PolicyResponse, ClaimResponse } from '@/lib/api'
import { formatCurrency, formatDate, getTriggerLabel, getTriggerIcon } from '@/lib/utils'
import Link from 'next/link'

export default function DashboardPage() {
  const router = useRouter()
  const { worker, isAuthenticated, _hasHydrated } = useAuthStore()
  const { activePolicy, claims, setActivePolicy, setClaims, isLoading, setLoading } = useDashboardStore()
  const [error, setError] = useState('')

  useEffect(() => {
    // Wait for hydration before checking auth
    if (!_hasHydrated) return
    
    if (!isAuthenticated) {
      router.push('/login')
      return
    }

    const fetchData = async () => {
      setLoading(true)
      try {
        // Fetch active policy
        try {
          const policyRes = await policyApi.getActive()
          setActivePolicy(policyRes.data)
        } catch {
          setActivePolicy(null)
        }

        // Fetch claims
        if (worker?.id) {
          const claimsRes = await claimsApi.getAll(worker.id)
          setClaims(claimsRes.data)
        }
      } catch (err) {
        console.error('Failed to fetch dashboard data:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [isAuthenticated, worker, _hasHydrated])

  // Show loading while hydrating or not authenticated
  if (!_hasHydrated) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600" />
        </div>
      </DashboardLayout>
    )
  }

  if (!isAuthenticated) return null

  const totalPaidOut = claims
    .filter(c => c.status === 'PAID')
    .reduce((sum, c) => sum + c.amount, 0)

  const pendingClaims = claims.filter(c => ['PENDING', 'VALIDATING', 'APPROVED'].includes(c.status))

  return (
    <DashboardLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        {/* Welcome Section */}
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">
            Welcome back, {worker?.name?.split(' ')[0]}! 👋
          </h1>
          <p className="text-slate-600 mt-1">
            Here&apos;s an overview of your insurance coverage
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <motion.div
            whileHover={{ y: -2 }}
            className="card p-5"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-primary-100 rounded-xl flex items-center justify-center">
                <Shield className="w-5 h-5 text-primary-600" />
              </div>
              <div>
                <p className="text-sm text-slate-600">Coverage</p>
                <p className="text-xl font-bold text-slate-900">
                  {activePolicy ? formatCurrency(activePolicy.coverageLimit) : '₹0'}
                </p>
              </div>
            </div>
          </motion.div>

          <motion.div
            whileHover={{ y: -2 }}
            className="card p-5"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-accent-100 rounded-xl flex items-center justify-center">
                <Wallet className="w-5 h-5 text-accent-600" />
              </div>
              <div>
                <p className="text-sm text-slate-600">Total Paid</p>
                <p className="text-xl font-bold text-slate-900">
                  {formatCurrency(totalPaidOut)}
                </p>
              </div>
            </div>
          </motion.div>

          <motion.div
            whileHover={{ y: -2 }}
            className="card p-5"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-yellow-100 rounded-xl flex items-center justify-center">
                <AlertCircle className="w-5 h-5 text-yellow-600" />
              </div>
              <div>
                <p className="text-sm text-slate-600">Pending</p>
                <p className="text-xl font-bold text-slate-900">
                  {pendingClaims.length}
                </p>
              </div>
            </div>
          </motion.div>

          <motion.div
            whileHover={{ y: -2 }}
            className="card p-5"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center">
                <TrendingUp className="w-5 h-5 text-purple-600" />
              </div>
              <div>
                <p className="text-sm text-slate-600">Risk Score</p>
                <p className="text-xl font-bold text-slate-900">
                  {activePolicy ? `${(activePolicy.riskScore * 100).toFixed(0)}%` : 'N/A'}
                </p>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Main Content Grid */}
        <div className="grid lg:grid-cols-2 gap-6">
          {/* Active Policy Card */}
          <Card>
            <CardBody>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold text-slate-900">Active Policy</h2>
                <Link href="/policy">
                  <Button variant="ghost" size="sm">
                    View All <ChevronRight className="w-4 h-4" />
                  </Button>
                </Link>
              </div>

              {activePolicy ? (
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-gradient-to-r from-primary-50 to-accent-50 rounded-xl">
                    <div>
                      <p className="text-sm text-slate-600">Week {activePolicy.weekNumber}, {activePolicy.year}</p>
                      <p className="text-2xl font-bold text-primary-600">
                        {formatCurrency(activePolicy.coverageLimit)}
                      </p>
                      <p className="text-sm text-slate-500">Coverage Limit</p>
                    </div>
                    <StatusBadge status={activePolicy.status} />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-3 bg-slate-50 rounded-lg">
                      <p className="text-xs text-slate-500">Premium Paid</p>
                      <p className="font-semibold">{formatCurrency(activePolicy.premium)}</p>
                    </div>
                    <div className="p-3 bg-slate-50 rounded-lg">
                      <p className="text-xs text-slate-500">Valid Until</p>
                      <p className="font-semibold">{formatDate(activePolicy.endDate)}</p>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-8">
                  <CloudRain className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                  <p className="text-slate-600 mb-4">No active policy</p>
                  <Link href="/policy">
                    <Button variant="primary">Get Protected Now</Button>
                  </Link>
                </div>
              )}
            </CardBody>
          </Card>

          {/* Recent Claims */}
          <Card>
            <CardBody>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold text-slate-900">Recent Claims</h2>
                <Link href="/claims">
                  <Button variant="ghost" size="sm">
                    View All <ChevronRight className="w-4 h-4" />
                  </Button>
                </Link>
              </div>

              {claims.length > 0 ? (
                <div className="space-y-3">
                  {claims.slice(0, 3).map((claim) => (
                    <motion.div
                      key={claim.id}
                      whileHover={{ x: 4 }}
                      className="flex items-center justify-between p-4 bg-slate-50 rounded-xl cursor-pointer"
                      onClick={() => router.push(`/claims/${claim.id}`)}
                    >
                      <div className="flex items-center gap-3">
                        <span className="text-2xl">{getTriggerIcon(claim.triggerType)}</span>
                        <div>
                          <p className="font-medium text-slate-900">
                            {getTriggerLabel(claim.triggerType)}
                          </p>
                          <p className="text-sm text-slate-500">
                            {formatDate(claim.triggeredAt)}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold text-slate-900">
                          {formatCurrency(claim.amount)}
                        </p>
                        <StatusBadge status={claim.status} />
                      </div>
                    </motion.div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <AlertCircle className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                  <p className="text-slate-600">No claims yet</p>
                  <p className="text-sm text-slate-500 mt-1">
                    Claims are automatically created when disruptions occur
                  </p>
                </div>
              )}
            </CardBody>
          </Card>
        </div>

        {/* Location Info */}
        <Card className="mt-6">
          <CardBody>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
                <CloudRain className="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <h3 className="font-semibold text-slate-900">Monitoring Active</h3>
                <p className="text-sm text-slate-600">
                  We&apos;re monitoring weather conditions in {worker?.location?.city || 'your area'}. 
                  You&apos;ll be automatically compensated if disruptions occur.
                </p>
              </div>
            </div>
          </CardBody>
        </Card>
      </motion.div>
    </DashboardLayout>
  )
}
