import type { DashboardSummary, NewProduct, Product, ProductPage } from '../types'

const API_URL = import.meta.env.VITE_API_URL ?? '/api/v1'

export type Credentials = {
  username: string
  password: string
}

export type Session = {
  accessToken: string
  username: string
  roles: string[]
}

type TokenResponse = Session & {
  tokenType: 'Bearer'
  expiresIn: number
}

type ProblemBody = {
  detail?: string
  requestId?: string
  type?: string
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly requestId: string | null,
    readonly type: string | null,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function request<T>(path: string, accessToken: string | null, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options.headers,
    },
  })
  if (!response.ok) {
    const body = await response.json().catch(() => null) as ProblemBody | null
    const detail = body?.detail ?? `Request failed with status ${response.status}`
    const requestId = body?.requestId ?? response.headers.get('X-Request-Id')
    const message = requestId ? `${detail} (referencia: ${requestId})` : detail
    throw new ApiError(message, response.status, requestId, body?.type ?? null)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  login: (credentials: Credentials) =>
    request<TokenResponse>('/auth/token', null, { method: 'POST', body: JSON.stringify(credentials) }),
  summary: (accessToken: string) => request<DashboardSummary>('/dashboard/summary', accessToken),
  products: (accessToken: string, page: number, size = 10) =>
    request<ProductPage>(`/products?page=${page}&size=${size}&sort=name&direction=asc`, accessToken),
  createProduct: (accessToken: string, product: NewProduct) =>
    request<Product>('/products', accessToken, { method: 'POST', body: JSON.stringify(product) }),
}
