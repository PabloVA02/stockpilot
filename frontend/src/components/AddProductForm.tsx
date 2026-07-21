import { type FormEvent, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { api } from '../api/client'

export function AddProductForm({ accessToken }: { accessToken: string }) {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const mutation = useMutation({
    mutationFn: (form: HTMLFormElement) => {
      const data = new FormData(form)
      return api.createProduct(accessToken, {
        sku: String(data.get('sku')),
        name: String(data.get('name')),
        description: String(data.get('description')),
        unitPrice: Number(data.get('unitPrice')),
        initialStock: Number(data.get('initialStock')),
        reorderLevel: Number(data.get('reorderLevel')),
      })
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['products'] }),
        queryClient.invalidateQueries({ queryKey: ['summary'] }),
      ])
      setOpen(false)
    },
  })

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    mutation.mutate(event.currentTarget)
  }

  if (!open) {
    return <button className="button button--primary" onClick={() => setOpen(true)}>Nuevo producto</button>
  }

  return (
    <form className="product-form" onSubmit={submit}>
      <div className="form-heading">
        <div>
          <span className="eyebrow">Alta de catálogo</span>
          <h2>Nuevo producto</h2>
        </div>
        <button className="button button--quiet" type="button" onClick={() => setOpen(false)}>Cancelar</button>
      </div>
      <div className="form-grid">
        <label>SKU<input name="sku" required maxLength={40} placeholder="TECLADO-PRO" /></label>
        <label>Nombre<input name="name" required maxLength={120} placeholder="Teclado profesional" /></label>
        <label>Precio<input name="unitPrice" required min="0.01" step="0.01" type="number" /></label>
        <label>Stock inicial<input name="initialStock" required min="0" type="number" /></label>
        <label>Nivel de reposición<input name="reorderLevel" required min="0" type="number" /></label>
        <label className="form-grid__wide">Descripción<textarea name="description" maxLength={500} /></label>
      </div>
      {mutation.error && <p className="error-message">{mutation.error.message}</p>}
      <button className="button button--primary" disabled={mutation.isPending}>
        {mutation.isPending ? 'Guardando…' : 'Guardar producto'}
      </button>
    </form>
  )
}
