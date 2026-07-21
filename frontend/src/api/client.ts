import type { DashboardSummary, NewProduct, Product, ProductPage } from '../types'

const API_URL = import.meta.env.VITE_API_URL ?? '/api/v1'

export type Credentials = {
  username: string
  password: string
}

async function request<T>(path: string, credentials: Credentials, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      Authorization: `Basic ${btoa(`${credentials.username}:${credentials.password}`)}`,
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.detail ?? `Request failed with status ${response.status}`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  summary: (credentials: Credentials) => request<DashboardSummary>('/dashboard/summary', credentials),
  products: (credentials: Credentials) => request<ProductPage>('/products?size=50&sort=name,asc', credentials),
  createProduct: (credentials: Credentials, product: NewProduct) =>
    request<Product>('/products', credentials, { method: 'POST', body: JSON.stringify(product) }),
}
