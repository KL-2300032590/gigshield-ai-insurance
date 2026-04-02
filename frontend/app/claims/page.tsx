'use client'

import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useRouter } from 'next/navigation'
import { AlertCircle, CheckCircle, XCircle, Clock, ChevronRight } from 'lucide-react'
import { DashboardLayout } from '@/components/layout/DashboardLayout'
import { Card, CardBody } from '@/components/ui/Card'
import { StatusBadge } from '@/components/ui/Badge'
import { useAuthStore } from '@/lib/store'
import { claimsApi, ClaimResponse } from '@/lib/api'
import { formatCurrency, formatDate, formatDateTime, getTriggerLabel, getTriggerIcon } from '@/lib/utils'

export default function ClaimsPage() {
  const router = useRouter()
  const { worker, isAuthenticated, _hasHydrated } = useAuthStore()
  const [claims, setClaims] = useState<ClaimResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Wait for hydration before checking auth
    if (!_hasHydrated) return
    
    if (!isAuthenticated) {
      router.push('/login')
      return
    }

    const fetchClaims = async () => {
      if (!worker?.id) return
      
      try {
        const res = await claimsApi.getAll(worker.id)
        setClaims(res.data)
      } catch (err) {
        console.error('Failed to fetch claims:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchClaims()
  }, [isAuthenticated, worker, _hasHydrated])

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

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PAID':
        return <CheckCircle className="w-5 h-5 text-emerald-600" />
      case 'REJECTED':
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-600" />
      case 'APPROVED':
        return <CheckCircle className="w-5 h-5 text-blue-600" />
      default:
        return <Clock className="w-5 h-5 text-yellow-600" />
    }
  }

  return (
    <DashboardLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">Claims</h1>
          <p className="text-slate-600 mt-1">Track your automatic claim history</p>
        </div>

        {/* Summary Stats */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div className="card p-5">
            <p className="text-sm text-slate-600">Total Claims</p>
            <p className="text-2xl font-bold text-slate-900">{claims.length}</p>
          </div>
          <div className="card p-5">
            <p className="text-sm text-slate-600">Paid Out</p>
            <p className="text-2xl font-bold text-emerald-600">
              {formatCurrency(claims.filter(c => c.status === 'PAID').reduce((sum, c) => sum + c.amount, 0))}
            </p>
          </div>
          <div className="card p-5">
            <p className="text-sm text-slate-600">Pending</p>
            <p className="text-2xl font-bold text-yellow-600">
              {claims.filter(c => ['PENDING', 'VALIDATING', 'APPROVED'].includes(c.status)).length}
            </p>
          </div>
          <div className="card p-5">
            <p className="text-sm text-slate-600">Rejected</p>
            <p className="text-2xl font-bold text-red-600">
              {claims.filter(c => c.status === 'REJECTED').length}
            </p>
          </div>
        </div>

        {/* Claims List */}
        {claims.length > 0 ? (
          <div className="space-y-4">
            {claims.map((claim, index) => (
              <motion.div
                key={claim.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <Card hover>
                  <CardBody>
                    <div className="flex items-start justify-between">
                      <div className="flex items-start gap-4">
                        <div className="text-3xl">{getTriggerIcon(claim.triggerType)}</div>
                        <div>
                          <h3 className="font-semibold text-slate-900">
                            {getTriggerLabel(claim.triggerType)}
                          </h3>
                          <p className="text-sm text-slate-500">
                            Triggered: {formatDateTime(claim.triggeredAt)}
                          </p>
                          {claim.triggerData && (
                            <div className="mt-2 text-sm text-slate-600">
                              <p>
                                Measured: {claim.triggerData.value.toFixed(1)} 
                                (Threshold: {claim.triggerData.threshold})
                              </p>
                              <p>Location: {claim.triggerData.location}</p>
                            </div>
                          )}
                        </div>
                      </div>
                      
                      <div className="text-right">
                        <p className="text-xl font-bold text-slate-900">
                          {formatCurrency(claim.amount)}
                        </p>
                        <div className="flex items-center gap-2 mt-1">
                          {getStatusIcon(claim.status)}
                          <StatusBadge status={claim.status} />
                        </div>
                        {claim.statusReason && (
                          <p className="text-xs text-slate-500 mt-1 max-w-[200px]">
                            {claim.statusReason}
                          </p>
                        )}
                      </div>
                    </div>

                    {/* Timeline */}
                    <div className="mt-4 pt-4 border-t border-slate-100">
                      <div className="flex items-center gap-4 text-sm">
                        <div className="flex items-center gap-2 text-slate-500">
                          <div className="w-2 h-2 rounded-full bg-slate-300" />
                          Created: {formatDateTime(claim.createdAt)}
                        </div>
                        {claim.processedAt && (
                          <div className="flex items-center gap-2 text-slate-500">
                            <div className="w-2 h-2 rounded-full bg-primary-400" />
                            Processed: {formatDateTime(claim.processedAt)}
                          </div>
                        )}
                      </div>
                    </div>
                  </CardBody>
                </Card>
              </motion.div>
            ))}
          </div>
        ) : (
          <Card>
            <CardBody className="text-center py-12">
              <AlertCircle className="w-16 h-16 text-slate-300 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-slate-900 mb-2">No Claims Yet</h2>
              <p className="text-slate-600 max-w-md mx-auto">
                Claims are automatically created when environmental disruptions affect your 
                work area. Make sure you have an active policy to be protected.
              </p>
            </CardBody>
          </Card>
        )}
      </motion.div>
    </DashboardLayout>
  )
}
