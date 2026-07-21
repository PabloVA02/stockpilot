type Props = {
  label: string
  value: number
  tone?: 'default' | 'warning'
}

export function StatCard({ label, value, tone = 'default' }: Props) {
  return (
    <article className={`stat-card stat-card--${tone}`}>
      <span>{label}</span>
      <strong>{value.toLocaleString('es-ES')}</strong>
    </article>
  )
}
