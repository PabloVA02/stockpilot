import { type FormEvent, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { api, type Credentials } from './api/client'
import { AddProductForm } from './components/AddProductForm'
import { StatCard } from './components/StatCard'
import { StockBadge } from './components/StockBadge'
import './styles.css'

function Login({ onLogin }: { onLogin: (credentials: Credentials) => void }) {
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    onLogin({ username: String(data.get('username')), password: String(data.get('password')) })
  }

  return (
    <main className="login-shell">
      <section className="login-card">
        <div className="brand-mark">SP</div>
        <span className="eyebrow">Inventory operations</span>
        <h1>StockPilot</h1>
        <p>Control de catálogo, existencias y alertas de reposición en una única vista.</p>
        <form onSubmit={submit}>
          <label>Usuario<input name="username" required autoComplete="username" /></label>
          <label>Contraseña<input name="password" type="password" required autoComplete="current-password" /></label>
          <button className="button button--primary">Entrar al panel</button>
        </form>
      </section>
    </main>
  )
}

export default function App() {
  const [credentials, setCredentials] = useState<Credentials | null>(null)
  const summary = useQuery({
    queryKey: ['summary', credentials?.username],
    queryFn: () => api.summary(credentials!),
    enabled: Boolean(credentials),
    retry: false,
  })
  const products = useQuery({
    queryKey: ['products', credentials?.username],
    queryFn: () => api.products(credentials!),
    enabled: Boolean(credentials),
    retry: false,
  })

  if (!credentials) return <Login onLogin={setCredentials} />

  if (summary.isError || products.isError) {
    return (
      <main className="login-shell">
        <section className="login-card">
          <span className="eyebrow">No se pudo acceder</span>
          <h1>Revisa tus credenciales</h1>
          <p>{summary.error?.message ?? products.error?.message}</p>
          <button className="button button--primary" onClick={() => setCredentials(null)}>Volver</button>
        </section>
      </main>
    )
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span>SP</span><strong>StockPilot</strong></div>
        <nav>
          <a className="active" href="#overview">Resumen</a>
          <a href="#inventory">Inventario</a>
          <a href="/swagger-ui.html" target="_blank">API Docs</a>
        </nav>
        <div className="sidebar-footer">
          <span>Sesión de</span>
          <strong>{credentials.username}</strong>
          <button onClick={() => setCredentials(null)}>Cerrar sesión</button>
        </div>
      </aside>

      <main className="main-content">
        <header className="page-header" id="overview">
          <div>
            <span className="eyebrow">Operaciones de inventario</span>
            <h1>Resumen de existencias</h1>
            <p>Indicadores actualizados desde la API de StockPilot.</p>
          </div>
          {credentials.username === 'manager' && <AddProductForm credentials={credentials} />}
        </header>

        {summary.isPending ? <div className="loading">Cargando indicadores…</div> : (
          <section className="stats-grid">
            <StatCard label="Productos activos" value={summary.data?.activeProducts ?? 0} />
            <StatCard label="Unidades disponibles" value={summary.data?.availableUnits ?? 0} />
            <StatCard label="Necesitan reposición" value={summary.data?.lowStockProducts ?? 0} tone="warning" />
          </section>
        )}

        <section className="table-card" id="inventory">
          <div className="section-heading">
            <div>
              <span className="eyebrow">Catálogo</span>
              <h2>Inventario actual</h2>
            </div>
            <span>{products.data?.totalElements ?? 0} productos</span>
          </div>
          {products.isPending ? <div className="loading">Cargando productos…</div> : (
            <div className="table-scroll">
              <table>
                <thead><tr><th>Producto</th><th>SKU</th><th>Precio</th><th>Stock</th><th>Estado</th></tr></thead>
                <tbody>
                  {products.data?.content.map((product) => (
                    <tr key={product.id}>
                      <td><strong>{product.name}</strong><span>{product.description}</span></td>
                      <td><code>{product.sku}</code></td>
                      <td>{product.unitPrice.toLocaleString('es-ES', { style: 'currency', currency: 'EUR' })}</td>
                      <td>{product.currentStock}<small>mín. {product.reorderLevel}</small></td>
                      <td><StockBadge lowStock={product.lowStock} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}
