import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(amount)
}

export function formatDate(date: string | Date): string {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
  }).format(new Date(date))
}

export function formatDateTime(date: string | Date): string {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(date))
}

export function getStatusColor(status: string): string {
  const colors: Record<string, string> = {
    ACTIVE: 'status-active',
    PENDING: 'status-pending',
    VALIDATING: 'status-pending',
    APPROVED: 'status-approved',
    REJECTED: 'status-rejected',
    PAID: 'status-paid',
    SUCCESS: 'status-paid',
    FAILED: 'status-rejected',
    EXPIRED: 'bg-gray-100 text-gray-700',
    CLAIMED: 'bg-purple-100 text-purple-700',
  }
  return colors[status] || 'bg-gray-100 text-gray-700'
}

export function getTriggerLabel(type: string): string {
  const labels: Record<string, string> = {
    HEAVY_RAIN: 'Heavy Rainfall',
    FLOOD: 'Flooding',
    AIR_POLLUTION: 'Air Pollution',
    EXTREME_HEAT: 'Extreme Heat',
    EXTREME_COLD: 'Extreme Cold',
  }
  return labels[type] || type
}

export function getTriggerIcon(type: string): string {
  const icons: Record<string, string> = {
    HEAVY_RAIN: '🌧️',
    FLOOD: '🌊',
    AIR_POLLUTION: '💨',
    EXTREME_HEAT: '🔥',
    EXTREME_COLD: '❄️',
  }
  return icons[type] || '⚠️'
}
