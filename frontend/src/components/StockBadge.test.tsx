import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { StockBadge } from './StockBadge'

describe('StockBadge', () => {
  it('shows a replenishment warning for low stock', () => {
    render(<StockBadge lowStock />)
    expect(screen.getByText('Reponer')).toBeInTheDocument()
  })

  it('shows the available state when stock is healthy', () => {
    render(<StockBadge lowStock={false} />)
    expect(screen.getByText('Disponible')).toBeInTheDocument()
  })
})
