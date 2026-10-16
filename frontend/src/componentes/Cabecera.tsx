import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useSesion } from '../contexto/contextoSesion'
import { nombreRol } from '../utils/presentacion'

export function Cabecera() {
  const { usuario, cargando, salir } = useSesion()
  const navegar = useNavigate()
  const esStaff = usuario?.rol === 'tecnico' || usuario?.rol === 'administrador'

  async function cerrarSesion() {
    try {
      await salir()
    } finally {
      navegar('/')
    }
  }

  const clase = ({ isActive }: { isActive: boolean }) => (isActive ? 'enlace enlace--activo' : 'enlace')

  return (
    <header className="cabecera">
      <div className="contenedor cabecera__contenido">
        <Link to="/" className="marca">
          <span className="marca__icono" aria-hidden="true">
            M2
          </span>
          <span className="marca__texto">
            Mesa 2.0
            <small>Soporte IT · RoyalTown</small>
          </span>
        </Link>

        <nav aria-label="Navegación principal" className="navegacion">
          <NavLink to="/" end className={clase}>
            Centro de ayuda
          </NavLink>
          {usuario?.rol === 'empleado' ? (
            <NavLink to="/mis-tickets" className={clase}>
              Mis tickets
            </NavLink>
          ) : null}
          {esStaff ? (
            <NavLink to="/bandeja" className={clase}>
              Bandeja
            </NavLink>
          ) : null}
          {esStaff ? (
            <NavLink to="/panel" className={clase}>
              Panel
            </NavLink>
          ) : null}
          {usuario?.rol === 'administrador' ? (
            <NavLink to="/admin" className={clase}>
              KB · Administración
            </NavLink>
          ) : null}
        </nav>

        <div className="sesion">
          {cargando ? null : usuario ? (
            <>
              <span className="sesion__usuario">
                {usuario.nombre}
                <small>{nombreRol(usuario.rol)}</small>
              </span>
              <button type="button" className="boton boton--fantasma boton--pequeno" onClick={cerrarSesion}>
                Salir
              </button>
            </>
          ) : (
            <Link to="/login" className="boton boton--pequeno">
              Entrar
            </Link>
          )}
        </div>
      </div>
    </header>
  )
}
