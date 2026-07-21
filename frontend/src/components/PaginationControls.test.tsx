import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { PaginationControls } from './PaginationControls'

describe('PaginationControls', () => {
  it('moves to the next page and blocks going before the first page', () => {
    const onPageChange = vi.fn()
    render(<PaginationControls page={0} totalPages={3} onPageChange={onPageChange} />)

    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()
    expect(screen.getByText('Página 1 de 3')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Siguiente' }))

    expect(onPageChange).toHaveBeenCalledWith(1)
  })

  it('disables both directions when there are no results', () => {
    render(<PaginationControls page={0} totalPages={0} onPageChange={() => undefined} />)

    expect(screen.getByText('Sin resultados')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Siguiente' })).toBeDisabled()
  })
})
