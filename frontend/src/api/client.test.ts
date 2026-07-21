import { afterEach, describe, expect, it, vi } from 'vitest'
import { api, ApiError } from './client'

afterEach(() => vi.unstubAllGlobals())

describe('API error correlation', () => {
  it('keeps the Problem Details request ID and makes it visible in the message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 403,
      headers: { get: vi.fn().mockReturnValue('header-trace') },
      json: vi.fn().mockResolvedValue({
        detail: 'No tienes permiso.',
        requestId: 'body-trace-123',
        type: 'https://stockpilot.dev/problems/authorization',
      }),
    }))

    const error = await api.summary('token').catch((caught: unknown) => caught)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({
      message: 'No tienes permiso. (referencia: body-trace-123)',
      status: 403,
      requestId: 'body-trace-123',
      type: 'https://stockpilot.dev/problems/authorization',
    })
  })

  it('falls back to the response header when the body has no request ID', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 502,
      headers: { get: vi.fn().mockReturnValue('proxy-trace-456') },
      json: vi.fn().mockRejectedValue(new Error('not-json')),
    }))

    const error = await api.products('token', 0).catch((caught: unknown) => caught)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({
      message: 'Request failed with status 502 (referencia: proxy-trace-456)',
      status: 502,
      requestId: 'proxy-trace-456',
      type: null,
    })
  })
})
