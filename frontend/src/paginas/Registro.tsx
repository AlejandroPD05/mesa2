import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ErrorApi, mensajeDe } from '../api/cliente'
import { useSesion } from '../contexto/contextoSesion'
import { Aviso } from '../componentes/Avisos'
import { destinoPorRol } from '../utils/presentacion'

export function Registro() {
  const { usuario, registrar } = useSesion()
  const navegar = useNavigate()
  const [nombre, setNombre] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [erroresCampo, setErroresCampo] = useState<Record<string, string>>({})
  const [enviando, setEnviando] = useState(false)

  if (usuario) {
    return <Navigate to={destinoPorRol(usuario.rol)} replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setError(null)
    setErroresCampo({})
    setEnviando(true)
    try {
      const perfil = await registrar(nombre, email, password)
      navegar(destinoPorRol(perfil.rol), { replace: true })
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.problema.errores) {
        setErroresCampo(fallo.problema.errores)
      } else {
        setError(mensajeDe(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="dos-columnas">
      <section aria-labelledby="titulo-registro">
        <h1 id="titulo-registro">Crear cuenta</h1>
        <p className="sutil">
          Tu cuenta sirve para abrir incidencias, seguir su estado y valorar las soluciones.
        </p>

        {error ? <Aviso>{error}</Aviso> : null}

        <form className="formulario" onSubmit={enviar} noValidate>
          <div className="campo">
            <label htmlFor="nombre">Nombre y apellidos</label>
            <input
              id="nombre"
              type="text"
              autoComplete="name"
              required
              value={nombre}
              onChange={(evento) => setNombre(evento.target.value)}
              aria-invalid={erroresCampo.nombre ? true : undefined}
              aria-describedby={erroresCampo.nombre ? 'error-nombre' : undefined}
            />
            {erroresCampo.nombre ? (
              <p className="error-campo" id="error-nombre">
                {erroresCampo.nombre}
              </p>
            ) : null}
          </div>
          <div className="campo">
            <label htmlFor="email">Correo electrónico</label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              required
              value={email}
              onChange={(evento) => setEmail(evento.target.value)}
              aria-invalid={erroresCampo.email ? true : undefined}
              aria-describedby={erroresCampo.email ? 'error-email' : undefined}
            />
            {erroresCampo.email ? (
              <p className="error-campo" id="error-email">
                {erroresCampo.email}
              </p>
            ) : null}
          </div>
          <div className="campo">
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              autoComplete="new-password"
              required
              minLength={8}
              value={password}
              onChange={(evento) => setPassword(evento.target.value)}
              aria-invalid={erroresCampo.password ? true : undefined}
              aria-describedby={erroresCampo.password ? 'error-password' : undefined}
            />
            <p className="ayuda-campo">Al menos 8 caracteres.</p>
            {erroresCampo.password ? (
              <p className="error-campo" id="error-password">
                {erroresCampo.password}
              </p>
            ) : null}
          </div>
          <button type="submit" className="boton" disabled={enviando}>
            {enviando ? 'Creando cuenta…' : 'Crear cuenta'}
          </button>
        </form>

        <p className="sutil">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>.
        </p>
      </section>
    </div>
  )
}
