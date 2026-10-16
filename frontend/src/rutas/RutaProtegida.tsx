import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSesion } from '../contexto/contextoSesion'
import { Cargando } from '../componentes/Avisos'

/**
 * Envuelve las rutas privadas. Sin sesión redirige a /login recordando de
 * dónde venía el usuario; con `roles` se limita a los perfiles indicados.
 */
export function RutaProtegida({ roles }: { roles?: string[] }) {
  const { usuario, cargando } = useSesion()
  const ubicacion = useLocation()

  if (cargando) {
    return <Cargando etiqueta="Comprobando tu sesión…" />
  }
  if (!usuario) {
    return <Navigate to="/login" state={{ desde: ubicacion.pathname }} replace />
  }
  if (roles && !roles.includes(usuario.rol)) {
    return <Navigate to="/mis-tickets" replace />
  }
  return <Outlet />
}
