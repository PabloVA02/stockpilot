export type Product = {
  id: string
  sku: string
  name: string
  description: string | null
  unitPrice: number
  currentStock: number
  reorderLevel: number
  lowStock: boolean
  active: boolean
}

export type ProductPage = {
  content: Product[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export type DashboardSummary = {
  activeProducts: number
  lowStockProducts: number
  availableUnits: number
}

export type NewProduct = {
  sku: string
  name: string
  description: string
  unitPrice: number
  initialStock: number
  reorderLevel: number
}
