import { afterEach, describe, expect, it, vi } from 'vitest'
import { ErrorApi, mensajeDe, obtener, pedir } from '../api/cliente'

function respuesta(cuerpo: unknown, estado = 200) {
  return new Response(JSON.stringify(cuerpo), {
    status: estado,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('cliente HTTP', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('devuelve el JSON cuando la respuesta es correcta', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta({ ok: true })))
    const datos = await obtener<{ ok: boolean }>('/api/prueba')
    expect(datos.ok).toBe(true)
  })

  it('convierte un ProblemDetail en ErrorApi con su estado', async () => {
    const problema = { title: 'No autorizado', detail: 'Credenciales incorrectas', status: 401 }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta(problema, 401)))

    try {
      await obtener('/api/prueba')
      expect.unreachable('debería haber lanzado un ErrorApi')
    } catch (fallo) {
      expect(fallo).toBeInstanceOf(ErrorApi)
      const error = fallo as ErrorApi
      expect(error.estado).toBe(401)
      expect(error.message).toBe('Credenciales incorrectas')
    }
  })

  it('resume los errores de validación por campo en un mensaje', async () => {
    const problema = {
      status: 400,
      detail: 'Datos inválidos',
      errores: { email: 'El correo es obligatorio', password: 'La contraseña es obligatoria' },
    }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta(problema, 400)))

    try {
      await obtener('/api/prueba')
      expect.unreachable('debería haber lanzado un ErrorApi')
    } catch (fallo) {
      expect(mensajeDe(fallo)).toBe('El correo es obligatorio · La contraseña es obligatoria')
    }
  })

  it('una respuesta 204 se resuelve sin cuerpo', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })))
    await expect(pedir('/api/prueba')).resolves.toBeUndefined()
  })
})
