import React from 'react'
import { render, screen } from '@testing-library/react'
import { Card, CardHeader, CardBody, CardFooter } from '@/components/ui/Card'

describe('Card Components', () => {
  describe('Card', () => {
    it('renders children correctly', () => {
      render(<Card>Test Content</Card>)
      expect(screen.getByText('Test Content')).toBeInTheDocument()
    })

    it('applies custom className', () => {
      render(<Card className="custom-class">Content</Card>)
      const content = screen.getByText('Content')
      // Content is directly inside the card, get the parent which is the card itself
      expect(content.closest('.card')).toHaveClass('custom-class')
    })
  })

  describe('CardHeader', () => {
    it('renders children correctly', () => {
      render(<CardHeader>Header Content</CardHeader>)
      expect(screen.getByText('Header Content')).toBeInTheDocument()
    })
  })

  describe('CardBody', () => {
    it('renders children correctly', () => {
      render(<CardBody>Body Content</CardBody>)
      expect(screen.getByText('Body Content')).toBeInTheDocument()
    })
  })

  describe('CardFooter', () => {
    it('renders children correctly', () => {
      render(<CardFooter>Footer Content</CardFooter>)
      expect(screen.getByText('Footer Content')).toBeInTheDocument()
    })
  })

  describe('Full Card Composition', () => {
    it('renders complete card structure', () => {
      render(
        <Card>
          <CardHeader>Header</CardHeader>
          <CardBody>Body</CardBody>
          <CardFooter>Footer</CardFooter>
        </Card>
      )
      
      expect(screen.getByText('Header')).toBeInTheDocument()
      expect(screen.getByText('Body')).toBeInTheDocument()
      expect(screen.getByText('Footer')).toBeInTheDocument()
    })
  })
})
