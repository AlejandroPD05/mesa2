import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { enviar, obtener } from '../api/cliente'
import type { Usuario } from '../api/tipos'
import { ContextoSesion, type ValorSesion } from './contextoSesion'

/**
 * La sesión vive en una cookie httpOnly puesta por el backend; al arrancar se
 * pregunta a /api/auth/me para saber si esa cookie es válida. El contexto solo
 * guarda el perfil para pintar la interfaz (menús por rol, nombre…).
 */
export function SesionProveedor({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    let vivo = true
    obtener<Usuario>('/api/auth/me')
      .then((perfil) => {
        if (vivo) setUsuario(perfil)
      })
      .catch(() => {
        // 401 esperado cuando no hay sesión: no es un error que mostrar.
      })
      .finally(() => {
        if (vivo) setCargando(false)
      })
    return () => {
      vivo = false
    }
  }, [])

  const valor = useMemo<ValorSesion>(
    () => ({
      usuario,
      cargando,
      async entrar(email, password) {
        const perfil = await enviar<Usuario>('/api/auth/login', 'POST', { email, password })
        setUsuario(perfil)
        return perfil
      },
      async registrar(nombre, email, password) {
        const perfil = await enviar<Usuario>('/api/auth/registro', 'POST', { nombre, email, password })
        setUsuario(perfil)
        return perfil
      },
      async salir() {
        await enviar<void>('/api/auth/logout', 'POST')
        setUsuario(null)
      },
    }),
    [usuario, cargando],
  )

  return <ContextoSesion.Provider value={valor}>{children}</ContextoSesion.Provider>
}
