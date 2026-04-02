'use client'

import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useRouter } from 'next/navigation'
import { Shield, Plus, Calendar, TrendingUp, CheckCircle } from 'lucide-react'
import { DashboardLayout } from '@/components/layout/DashboardLayout'
import { Card, CardBody, CardHeader } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { StatusBadge } from '@/components/ui/Badge'
import { useAuthStore } from '@/lib/store'
import { policyApi, PolicyResponse } from '@/lib/api'
import { formatCurrency, formatDate } from '@/lib/utils'

export default function PolicyPage() {
  const router = useRouter()
  const { worker, isAuthenticated, _hasHydrated } = useAuthStore()
  const [policies, setPolicies] = useState<PolicyResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [purchasing, setPurchasing] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    // Wait for hydration before checking auth
    if (!_hasHydrated) return
    
    if (!isAuthenticated) {
      router.push('/login')
      return
    }

    const fetchPolicies = async () => {
      try {
        const res = await policyApi.getAll()
        setPolicies(res.data)
      } catch (err) {
        console.error('Failed to fetch policies:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchPolicies()
  }, [isAuthenticated, _hasHydrated])

  const handlePurchase = async () => {
    setPurchasing(true)
    setError('')

    try {
      await policyApi.purchase({})
      const res = await policyApi.getAll()
      setPolicies(res.data)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to purchase policy')
    } finally {
      setPurchasing(false)
    }
  }

  // Show loading while hydrating
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

  const activePolicy = policies.find(p => p.status === 'ACTIVE')
  const pastPolicies = policies.filter(p => p.status !== 'ACTIVE')

  return (
    <DashboardLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">My Policy</h1>
            <p className="text-slate-600 mt-1">Manage your weekly insurance coverage</p>
          </div>
          {!activePolicy && (
            <Button onClick={handlePurchase} loading={purchasing}>
              <Plus className="w-4 h-4" /> Buy Policy
            </Button>
          )}
        </div>

        {error && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-red-50 text-red-600 px-4 py-3 rounded-lg mb-6"
          >
            {error}
          </motion.div>
        )}

        {/* Active Policy */}
        {activePolicy ? (
          <Card className="mb-8 border-2 border-primary-200">
            <CardHeader className="bg-gradient-to-r from-primary-50 to-accent-50">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-white rounded-xl flex items-center justify-center shadow">
                    <Shield className="w-6 h-6 text-primary-600" />
                  </div>
                  <div>
                    <h2 className="text-lg font-semibold text-slate-900">Active Policy</h2>
                    <p className="text-sm text-slate-600">Week {activePolicy.weekNumber}, {activePolicy.year}</p>
                  </div>
                </div>
                <StatusBadge status={activePolicy.status} />
              </div>
            </CardHeader>
            <CardBody>
              <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
                <div>
                  <p className="text-sm text-slate-500 mb-1">Coverage Limit</p>
                  <p className="text-2xl font-bold text-primary-600">
                    {formatCurrency(activePolicy.coverageLimit)}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-500 mb-1">Premium Paid</p>
                  <p className="text-2xl font-bold text-slate-900">
                    {formatCurrency(activePolicy.premium)}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-slate-500 mb-1">Risk Score</p>
                  <div className="flex items-center gap-2">
                    <TrendingUp className="w-5 h-5 text-accent-600" />
                    <p className="text-2xl font-bold text-slate-900">
                      {(activePolicy.riskScore * 100).toFixed(0)}%
                    </p>
                  </div>
                </div>
                <div>
                  <p className="text-sm text-slate-500 mb-1">Valid Period</p>
                  <div className="flex items-center gap-2">
                    <Calendar className="w-5 h-5 text-slate-400" />
                    <p className="font-semibold text-slate-900">
                      {formatDate(activePolicy.startDate)} - {formatDate(activePolicy.endDate)}
                    </p>
                  </div>
                </div>
              </div>

              <div className="mt-6 p-4 bg-accent-50 rounded-xl">
                <div className="flex items-start gap-3">
                  <CheckCircle className="w-5 h-5 text-accent-600 mt-0.5" />
                  <div>
                    <p className="font-medium text-accent-900">You&apos;re Protected!</p>
                    <p className="text-sm text-accent-700">
                      Your policy covers disruptions from heavy rain, floods, air pollution, and extreme temperatures.
                      Claims are processed automatically when triggers are detected.
                    </p>
                  </div>
                </div>
              </div>
            </CardBody>
          </Card>
        ) : (
          <Card className="mb-8">
            <CardBody className="text-center py-12">
              <Shield className="w-16 h-16 text-slate-300 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-slate-900 mb-2">No Active Policy</h2>
              <p className="text-slate-600 mb-6 max-w-md mx-auto">
                Get protected from weather disruptions. Purchase a weekly policy for just ₹20
                and get up to ₹800 coverage.
              </p>
              <Button onClick={handlePurchase} loading={purchasing} size="lg">
                <Plus className="w-5 h-5" /> Purchase Weekly Policy
              </Button>
            </CardBody>
          </Card>
        )}

        {/* Policy History */}
        {pastPolicies.length > 0 && (
          <div>
            <h2 className="text-lg font-semibold text-slate-900 mb-4">Policy History</h2>
            <div className="space-y-4">
              {pastPolicies.map((policy) => (
                <motion.div
                  key={policy.id}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  whileHover={{ scale: 1.01 }}
                  className="card p-5"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 bg-slate-100 rounded-xl flex items-center justify-center">
                        <Shield className="w-5 h-5 text-slate-400" />
                      </div>
                      <div>
                        <p className="font-medium text-slate-900">
                          Week {policy.weekNumber}, {policy.year}
                        </p>
                        <p className="text-sm text-slate-500">
                          {formatDate(policy.startDate)} - {formatDate(policy.endDate)}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <StatusBadge status={policy.status} />
                      <p className="text-sm text-slate-500 mt-1">
                        Premium: {formatCurrency(policy.premium)}
                      </p>
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        )}
      </motion.div>
    </DashboardLayout>
  )
}
