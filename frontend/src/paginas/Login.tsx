import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { mensajeDe } from '../api/cliente'
import { useSesion } from '../contexto/contextoSesion'
import { Aviso } from '../componentes/Avisos'
import { destinoPorRol } from '../utils/presentacion'

const CUENTAS_DEMO = [
  { rol: 'Empleada', email: 'marta.sanchez@royaltown.local' },
  { rol: 'Técnica', email: 'laura.gomez@royaltown.local' },
  { rol: 'Administrador', email: 'alejandro.ortiz@royaltown.local' },
]

export function Login() {
  const { usuario, entrar } = useSesion()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const desde = (ubicacion.state as { desde?: string } | null)?.desde

  if (usuario) {
    return <Navigate to={desde ?? destinoPorRol(usuario.rol)} replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      const perfil = await entrar(email, password)
      navegar(desde ?? destinoPorRol(perfil.rol), { replace: true })
    } catch (fallo) {
      setError(mensajeDe(fallo))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="dos-columnas">
      <section aria-labelledby="titulo-login">
        <h1 id="titulo-login">Iniciar sesión</h1>
        <p className="sutil">Entra con tu cuenta de RoyalTown para abrir y seguir tus tickets.</p>

        {error ? <Aviso>{error}</Aviso> : null}

        <form className="formulario" onSubmit={enviar} noValidate>
          <div className="campo">
            <label htmlFor="email">Correo electrónico</label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              required
              value={email}
              onChange={(evento) => setEmail(evento.target.value)}
            />
          </div>
          <div className="campo">
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(evento) => setPassword(evento.target.value)}
            />
          </div>
          <button type="submit" className="boton" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Iniciar sesión'}
          </button>
        </form>

        <p className="sutil">
          ¿Sin cuenta? <Link to="/registro">Regístrate</Link>.
        </p>
      </section>

      <aside className="tarjeta" aria-labelledby="cuentas-demo">
        <h2 id="cuentas-demo">Cuentas de demostración</h2>
        <p className="sutil">Los tres perfiles del portal, con la misma contraseña.</p>
        <ul className="lista-demo">
          {CUENTAS_DEMO.map((cuenta) => (
            <li key={cuenta.email}>
              <div>
                <strong>{cuenta.rol}</strong>
                <small>{cuenta.email}</small>
              </div>
              <button
                type="button"
                className="boton boton--secundario boton--pequeno"
                onClick={() => {
                  setEmail(cuenta.email)
                  setPassword('royaltown2026')
                }}
              >
                Rellenar
              </button>
            </li>
          ))}
        </ul>
        <p className="sutil">
          Contraseña para todas: <code>royaltown2026</code>
        </p>
      </aside>
    </div>
  )
}
