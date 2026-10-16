import { useState } from 'react'
import Markdown from 'react-markdown'
import { Link, useParams } from 'react-router-dom'
import { useArticulo, useFeedback } from '../api/consultas'
import { mensajeDe } from '../api/cliente'
import { useSesion } from '../contexto/contextoSesion'
import { Aviso, Cargando } from '../componentes/Avisos'
import { formatearFecha } from '../utils/fechas'

export function Articulo() {
  const { slug = '' } = useParams()
  const { usuario } = useSesion()
  const { data: articulo, isPending, isError, error } = useArticulo(slug)
  const feedback = useFeedback(slug, articulo?.id ?? 0)
  const [votado, setVotado] = useState(false)

  async function votar(util: boolean) {
    try {
      await feedback.mutateAsync(util)
      setVotado(true)
    } catch {
      // El aviso de error se pinta debajo con feedback.error.
    }
  }

  if (isPending) return <Cargando etiqueta="Abriendo el artículo…" />

  if (isError || !articulo) {
    return (
      <div className="pila">
        <Aviso>{mensajeDe(error)}</Aviso>
        <p>
          <Link to="/">&larr; Volver al centro de ayuda</Link>
        </p>
      </div>
    )
  }

  return (
    <div className="pila">
      <p className="sutil">
        <Link to="/">&larr; Centro de ayuda</Link>
      </p>

      <article className="articulo">
        <header>
          <span className="tarjeta__meta">{articulo.categoria}</span>
          <h1>{articulo.titulo}</h1>
          <p className="articulo__resumen">{articulo.resumen}</p>
          <p className="sutil">
            Actualizado el {formatearFecha(articulo.fechaCreacion)} · {articulo.visitas}{' '}
            {articulo.visitas === 1 ? 'visita' : 'visitas'}
          </p>
        </header>

        <div className="prosa">
          <Markdown>{articulo.contenido}</Markdown>
        </div>

        <footer className="feedback">
          <p>
            <strong>¿Te ha servido este artículo?</strong>
          </p>
          {votado ? (
            <p role="status" className="feedback__gracias">
              Gracias por tu valoración. Marca útil: {articulo.votosUtil} · No útil:{' '}
              {articulo.votosNoUtil}
            </p>
          ) : (
            <div className="fila">
              <button
                type="button"
                className="boton"
                disabled={feedback.isPending}
                onClick={() => votar(true)}
              >
                Sí, me ha ayudado
              </button>
              <button
                type="button"
                className="boton boton--secundario"
                disabled={feedback.isPending}
                onClick={() => votar(false)}
              >
                No me ha ayudado
              </button>
            </div>
          )}
          {feedback.isError ? <Aviso>{mensajeDe(feedback.error)}</Aviso> : null}
        </footer>
      </article>

      <section className="banda">
        <strong>¿Sigues con el problema?</strong>{' '}
        {usuario ? (
          <Link to="/tickets/nuevo">Abre un ticket</Link>
        ) : (
          <Link to="/login">Inicia sesión</Link>
        )}{' '}
        y el equipo de soporte lo retomará contigo.
      </section>
    </div>
  )
}
