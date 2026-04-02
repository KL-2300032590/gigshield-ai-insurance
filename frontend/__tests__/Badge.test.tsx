import React from 'react'
import { render, screen } from '@testing-library/react'
import { StatusBadge } from '@/components/ui/Badge'

describe('StatusBadge', () => {
  it('renders with correct text', () => {
    render(<StatusBadge status="ACTIVE" />)
    expect(screen.getByText('ACTIVE')).toBeInTheDocument()
  })

  it('applies correct styling for ACTIVE status', () => {
    render(<StatusBadge status="ACTIVE" />)
    const badge = screen.getByText('ACTIVE')
    expect(badge).toHaveClass('status-active')
  })

  it('applies correct styling for PENDING status', () => {
    render(<StatusBadge status="PENDING" />)
    const badge = screen.getByText('PENDING')
    expect(badge).toHaveClass('status-pending')
  })

  it('applies correct styling for PAID status', () => {
    render(<StatusBadge status="PAID" />)
    const badge = screen.getByText('PAID')
    expect(badge).toHaveClass('status-paid')
  })

  it('applies additional className when provided', () => {
    render(<StatusBadge status="ACTIVE" className="custom-class" />)
    const badge = screen.getByText('ACTIVE')
    expect(badge).toHaveClass('custom-class')
  })
})
