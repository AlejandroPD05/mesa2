import { createContext, useContext } from 'react'
import type { Usuario } from '../api/tipos'

export interface ValorSesion {
  /** Usuario conectado, o null si no hay sesión. */
  usuario: Usuario | null
  /** true mientras se comprueba la cookie contra /api/auth/me al arrancar. */
  cargando: boolean
  entrar: (email: string, password: string) => Promise<Usuario>
  registrar: (nombre: string, email: string, password: string) => Promise<Usuario>
  salir: () => Promise<void>
}

export const ContextoSesion = createContext<ValorSesion | null>(null)

export function useSesion(): ValorSesion {
  const valor = useContext(ContextoSesion)
  if (!valor) {
    throw new Error('useSesion debe usarse dentro de <SesionProveedor>')
  }
  return valor
}
