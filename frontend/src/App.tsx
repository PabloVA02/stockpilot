import { type FormEvent, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { api, type Session } from './api/client'
import { AddProductForm } from './components/AddProductForm'
import { PaginationControls } from './components/PaginationControls'
import { StatCard } from './components/StatCard'
import { StockBadge } from './components/StockBadge'
import './styles.css'

function Login({ onLogin }: { onLogin: (session: Session) => void }) {
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setError(null)
    setSubmitting(true)
    try {
      const session = await api.login({
        username: String(data.get('username')),
        password: String(data.get('password')),
      })
      onLogin(session)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'No se pudo iniciar sesión')
    } finally {
      setSubmitting(false)
    }
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
          {error && <p className="error-message">{error}</p>}
          <button className="button button--primary" disabled={submitting}>
            {submitting ? 'Accediendo…' : 'Entrar al panel'}
          </button>
        </form>
      </section>
    </main>
  )
}

export default function App() {
  const [session, setSession] = useState<Session | null>(null)
  const [productPage, setProductPage] = useState(0)
  const summary = useQuery({
    queryKey: ['summary', session?.username],
    queryFn: () => api.summary(session!.accessToken),
    enabled: Boolean(session),
    retry: false,
  })
  const products = useQuery({
    queryKey: ['products', session?.username, productPage],
    queryFn: () => api.products(session!.accessToken, productPage),
    enabled: Boolean(session),
    retry: false,
  })

  function login(nextSession: Session) {
    setProductPage(0)
    setSession(nextSession)
  }

  function logout() {
    setProductPage(0)
    setSession(null)
  }

  if (!session) return <Login onLogin={login} />

  if (summary.isError || products.isError) {
    return (
      <main className="login-shell">
        <section className="login-card">
          <span className="eyebrow">No se pudo acceder</span>
          <h1>No se pudo cargar el panel</h1>
          <p>{summary.error?.message ?? products.error?.message}</p>
          <button className="button button--primary" onClick={logout}>Volver</button>
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
          <a href="/swagger-ui.html" target="_blank" rel="noreferrer">API Docs</a>
        </nav>
        <div className="sidebar-footer">
          <span>Sesión de</span>
          <strong>{session.username}</strong>
          <button onClick={logout}>Cerrar sesión</button>
        </div>
      </aside>

      <main className="main-content">
        <header className="page-header" id="overview">
          <div>
            <span className="eyebrow">Operaciones de inventario</span>
            <h1>Resumen de existencias</h1>
            <p>Indicadores actualizados desde la API de StockPilot.</p>
          </div>
          {session.roles.includes('MANAGER') && <AddProductForm accessToken={session.accessToken} />}
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
            <>
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
              <PaginationControls
                page={products.data?.number ?? productPage}
                totalPages={products.data?.totalPages ?? 0}
                disabled={products.isFetching}
                onPageChange={setProductPage}
              />
            </>
          )}
        </section>
      </main>
    </div>
  )
}
