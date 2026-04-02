'use client'

import { cn, getStatusColor } from '@/lib/utils'

interface BadgeProps {
  status: string
  className?: string
}

export function StatusBadge({ status, className }: BadgeProps) {
  return (
    <span className={cn('status-badge', getStatusColor(status), className)}>
      {status}
    </span>
  )
}
