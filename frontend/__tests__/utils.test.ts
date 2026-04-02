import { formatCurrency, formatDate, getStatusColor, getTriggerLabel, getTriggerIcon } from '@/lib/utils'

describe('Utility Functions', () => {
  describe('formatCurrency', () => {
    it('formats Indian Rupees correctly', () => {
      expect(formatCurrency(800)).toBe('₹800')
      expect(formatCurrency(1000)).toBe('₹1,000')
      expect(formatCurrency(20)).toBe('₹20')
    })
  })

  describe('formatDate', () => {
    it('formats date correctly', () => {
      const date = '2024-01-15'
      const formatted = formatDate(date)
      expect(formatted).toContain('Jan')
      expect(formatted).toContain('2024')
    })
  })

  describe('getStatusColor', () => {
    it('returns correct color classes for status', () => {
      expect(getStatusColor('ACTIVE')).toBe('status-active')
      expect(getStatusColor('PENDING')).toBe('status-pending')
      expect(getStatusColor('APPROVED')).toBe('status-approved')
      expect(getStatusColor('REJECTED')).toBe('status-rejected')
      expect(getStatusColor('PAID')).toBe('status-paid')
    })

    it('returns default color for unknown status', () => {
      expect(getStatusColor('UNKNOWN')).toBe('bg-gray-100 text-gray-700')
    })
  })

  describe('getTriggerLabel', () => {
    it('returns readable labels for trigger types', () => {
      expect(getTriggerLabel('HEAVY_RAIN')).toBe('Heavy Rainfall')
      expect(getTriggerLabel('FLOOD')).toBe('Flooding')
      expect(getTriggerLabel('AIR_POLLUTION')).toBe('Air Pollution')
      expect(getTriggerLabel('EXTREME_HEAT')).toBe('Extreme Heat')
      expect(getTriggerLabel('EXTREME_COLD')).toBe('Extreme Cold')
    })

    it('returns the original value for unknown types', () => {
      expect(getTriggerLabel('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getTriggerIcon', () => {
    it('returns appropriate emoji for trigger types', () => {
      expect(getTriggerIcon('HEAVY_RAIN')).toBe('🌧️')
      expect(getTriggerIcon('FLOOD')).toBe('🌊')
      expect(getTriggerIcon('AIR_POLLUTION')).toBe('💨')
      expect(getTriggerIcon('EXTREME_HEAT')).toBe('🔥')
      expect(getTriggerIcon('EXTREME_COLD')).toBe('❄️')
    })

    it('returns warning emoji for unknown types', () => {
      expect(getTriggerIcon('UNKNOWN')).toBe('⚠️')
    })
  })
})
