'use client'

import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useRouter } from 'next/navigation'
import { Wallet, CheckCircle, XCircle, Clock, ArrowRight } from 'lucide-react'
import { DashboardLayout } from '@/components/layout/DashboardLayout'
import { Card, CardBody } from '@/components/ui/Card'
import { StatusBadge } from '@/components/ui/Badge'
import { useAuthStore } from '@/lib/store'
import { formatCurrency, formatDateTime } from '@/lib/utils'

// Mock payouts data (would come from API in production)
interface PayoutData {
  id: string
  claimId: string
  amount: number
  transactionId?: string
  paymentMethod: string
  status: 'INITIATED' | 'PROCESSING' | 'SUCCESS' | 'FAILED'
  initiatedAt: string
  completedAt?: string
}

export default function PayoutsPage() {
  const router = useRouter()
  const { isAuthenticated } = useAuthStore()
  const [payouts, setPayouts] = useState<PayoutData[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login')
      return
    }

    // Simulated payouts data
    setPayouts([])
    setLoading(false)
  }, [isAuthenticated])

  if (!isAuthenticated) return null

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return <CheckCircle className="w-5 h-5 text-emerald-600" />
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-600" />
      default:
        return <Clock className="w-5 h-5 text-yellow-600" />
    }
  }

  const totalReceived = payouts
    .filter(p => p.status === 'SUCCESS')
    .reduce((sum, p) => sum + p.amount, 0)

  return (
    <DashboardLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-slate-900">Payouts</h1>
          <p className="text-slate-600 mt-1">Track your compensation payments</p>
        </div>

        {/* Summary Card */}
        <Card className="mb-8 bg-gradient-to-r from-accent-500 to-accent-600">
          <CardBody>
            <div className="flex items-center justify-between text-white">
              <div>
                <p className="text-accent-100">Total Received</p>
                <p className="text-4xl font-bold mt-1">{formatCurrency(totalReceived)}</p>
              </div>
              <div className="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center">
                <Wallet className="w-8 h-8 text-white" />
              </div>
            </div>
          </CardBody>
        </Card>

        {/* Summary Stats */}
        <div className="grid grid-cols-3 gap-4 mb-8">
          <div className="card p-5">
            <p className="text-sm text-slate-600">Successful</p>
            <p className="text-2xl font-bold text-emerald-600">
              {payouts.filter(p => p.status === 'SUCCESS').length}
            </p>
          </div>
          <div className="card p-5">
            <p className="text-sm text-slate-600">Processing</p>
            <p className="text-2xl font-bold text-yellow-600">
              {payouts.filter(p => ['INITIATED', 'PROCESSING'].includes(p.status)).length}
            </p>
          </div>
          <div className="card p-5">
            <p className="text-sm text-slate-600">Failed</p>
            <p className="text-2xl font-bold text-red-600">
              {payouts.filter(p => p.status === 'FAILED').length}
            </p>
          </div>
        </div>

        {/* Payouts List */}
        {payouts.length > 0 ? (
          <div className="space-y-4">
            {payouts.map((payout, index) => (
              <motion.div
                key={payout.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <Card>
                  <CardBody>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-4">
                        <div className="w-12 h-12 bg-accent-100 rounded-xl flex items-center justify-center">
                          <Wallet className="w-6 h-6 text-accent-600" />
                        </div>
                        <div>
                          <p className="font-semibold text-slate-900">
                            Claim Payout
                          </p>
                          <p className="text-sm text-slate-500">
                            {formatDateTime(payout.initiatedAt)}
                          </p>
                          {payout.transactionId && (
                            <p className="text-xs text-slate-400 mt-1">
                              TXN: {payout.transactionId}
                            </p>
                          )}
                        </div>
                      </div>
                      
                      <div className="text-right">
                        <p className="text-xl font-bold text-slate-900">
                          {formatCurrency(payout.amount)}
                        </p>
                        <div className="flex items-center justify-end gap-2 mt-1">
                          {getStatusIcon(payout.status)}
                          <StatusBadge status={payout.status} />
                        </div>
                      </div>
                    </div>

                    {payout.status === 'SUCCESS' && (
                      <div className="mt-4 pt-4 border-t border-slate-100 flex items-center gap-2 text-sm text-accent-600">
                        <CheckCircle className="w-4 h-4" />
                        Credited to your {payout.paymentMethod} account
                        {payout.completedAt && ` on ${formatDateTime(payout.completedAt)}`}
                      </div>
                    )}
                  </CardBody>
                </Card>
              </motion.div>
            ))}
          </div>
        ) : (
          <Card>
            <CardBody className="text-center py-12">
              <Wallet className="w-16 h-16 text-slate-300 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-slate-900 mb-2">No Payouts Yet</h2>
              <p className="text-slate-600 max-w-md mx-auto">
                When your claims are approved, payouts will appear here. 
                Payments are processed automatically to your registered account.
              </p>
            </CardBody>
          </Card>
        )}

        {/* Payment Method Info */}
        <Card className="mt-6">
          <CardBody>
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                <ArrowRight className="w-5 h-5 text-primary-600" />
              </div>
              <div>
                <h3 className="font-semibold text-slate-900">Payment Method</h3>
                <p className="text-sm text-slate-600 mt-1">
                  Payouts are automatically sent via UPI to your registered phone number.
                  Payments are typically processed within 1 hour of claim approval.
                </p>
              </div>
            </div>
          </CardBody>
        </Card>
      </motion.div>
    </DashboardLayout>
  )
}
