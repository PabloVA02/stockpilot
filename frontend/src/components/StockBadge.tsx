export function StockBadge({ lowStock }: { lowStock: boolean }) {
  return (
    <span className={lowStock ? 'badge badge--warning' : 'badge badge--ok'}>
      {lowStock ? 'Reponer' : 'Disponible'}
    </span>
  )
}
