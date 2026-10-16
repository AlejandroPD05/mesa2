/**
 * Cliente HTTP mínimo sobre fetch. La sesión vive en una cookie httpOnly que
 * el navegador envía sola (mismo origen); aquí no se toca ningún token.
 * Los errores del backend llegan como ProblemDetail (RFC 7807) y se convierten
 * en ErrorApi para que las pantallas puedan mostrar `detail`, los `errores` de
 * validación por campo o las `transicionesValidas` de un estado bloqueado.
 */

export interface Problema {
  type?: string
  title?: string
  status?: number
  detail?: string
  errores?: Record<string, string>
  estadoActual?: string
  transicionesValidas?: string[]
  campo?: string
}

export class ErrorApi extends Error {
  readonly estado: number
  readonly problema: Problema

  constructor(estado: number, problema: Problema) {
    super(problema.detail ?? problema.title ?? `Error ${estado}`)
    this.name = 'ErrorApi'
    this.estado = estado
    this.problema = problema
  }
}

export async function pedir<T>(ruta: string, opciones: RequestInit = {}): Promise<T> {
  const respuesta = await fetch(ruta, {
    credentials: 'same-origin',
    ...opciones,
    headers: {
      ...(opciones.body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(opciones.headers ?? {}),
    },
  })

  if (!respuesta.ok) {
    let problema: Problema
    try {
      problema = (await respuesta.json()) as Problema
    } catch {
      problema = { detail: `El servidor respondió con un error ${respuesta.status}` }
    }
    throw new ErrorApi(respuesta.status, problema)
  }

  if (respuesta.status === 204) {
    return undefined as T
  }
  return (await respuesta.json()) as T
}

export function obtener<T>(ruta: string): Promise<T> {
  return pedir<T>(ruta)
}

export function enviar<T>(ruta: string, metodo: 'POST' | 'PUT' | 'PATCH' | 'DELETE', cuerpo?: unknown): Promise<T> {
  return pedir<T>(ruta, {
    method: metodo,
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
  })
}

/** Mensaje legible de cualquier error, con los detalles de validación si los hay. */
export function mensajeDe(error: unknown): string {
  if (error instanceof ErrorApi) {
    const porCampo = error.problema.errores
    if (porCampo && Object.keys(porCampo).length > 0) {
      return Object.values(porCampo).join(' · ')
    }
    return error.message
  }
  if (error instanceof Error) {
    return 'No se pudo conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo.'
  }
  return 'Ha ocurrido un error inesperado.'
}
