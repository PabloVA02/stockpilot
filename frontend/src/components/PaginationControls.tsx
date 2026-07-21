type PaginationControlsProps = {
  page: number
  totalPages: number
  disabled?: boolean
  onPageChange: (page: number) => void
}

export function PaginationControls({ page, totalPages, disabled = false, onPageChange }: PaginationControlsProps) {
  const hasResults = totalPages > 0
  const label = hasResults ? `Página ${page + 1} de ${totalPages}` : 'Sin resultados'

  return (
    <nav className="pagination" aria-label="Paginación del inventario">
      <button
        className="button button--quiet"
        type="button"
        disabled={disabled || page <= 0}
        onClick={() => onPageChange(page - 1)}
      >
        Anterior
      </button>
      <span aria-live="polite">{label}</span>
      <button
        className="button button--quiet"
        type="button"
        disabled={disabled || !hasResults || page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Siguiente
      </button>
    </nav>
  )
}
