import { useDeferredValue, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useArticulos, useCatalogo, useCrearTicket } from '../api/consultas'
import { ErrorApi, mensajeDe } from '../api/cliente'
import { Aviso, Cargando } from '../componentes/Avisos'

/**
 * Alta de incidencia con autoservicio primero: mientras se escribe el título,
 * se buscan artículos parecidos en el centro de ayuda para intentar resolver
 * la duda antes de crear el ticket.
 */
export function NuevoTicket() {
  const navegar = useNavigate()
  const { data: catalogo, isPending: cargandoCatalogo, isError: errorCatalogo, error: falloCatalogo } =
    useCatalogo()
  const crear = useCrearTicket()

  const [titulo, setTitulo] = useState('')
  const [idCategoria, setIdCategoria] = useState(0)
  const [idPrioridad, setIdPrioridad] = useState(0)
  const [descripcion, setDescripcion] = useState('')
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)

  const tituloDiferido = useDeferredValue(titulo.trim())
  const hayBusqueda = tituloDiferido.length >= 4
  const {
    data: sugerencias,
    isFetching: buscandoSugerencias,
  } = useArticulos({ texto: tituloDiferido, activo: hayBusqueda })

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setError(null)
    setErrores({})
    try {
      const ticket = await crear.mutateAsync({
        titulo: titulo.trim(),
        descripcion: descripcion.trim(),
        idCategoria,
        idPrioridad,
      })
      navegar(`/tickets/${ticket.id}`, { replace: true })
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.problema.errores) {
        setErrores(fallo.problema.errores)
      } else {
        setError(mensajeDe(fallo))
      }
    }
  }

  return (
    <div className="dos-columnas">
      <section aria-labelledby="titulo-nuevo">
        <h1 id="titulo-nuevo">Nuevo ticket</h1>
        <p className="sutil">
          Cuéntanos qué ocurre con el mayor detalle posible: qué esperabas, qué pasó y desde cuándo.
        </p>

        {error ? <Aviso>{error}</Aviso> : null}

        {cargandoCatalogo ? <Cargando etiqueta="Preparando el formulario…" /> : null}
        {errorCatalogo ? <Aviso>{mensajeDe(falloCatalogo)}</Aviso> : null}

        {catalogo ? (
          <form className="formulario" onSubmit={enviar} noValidate>
            <div className="campo">
              <label htmlFor="titulo">Título</label>
              <input
                id="titulo"
                type="text"
                required
                maxLength={150}
                placeholder="Ej.: La impresora de contabilidad no imprime"
                value={titulo}
                onChange={(evento) => setTitulo(evento.target.value)}
                aria-invalid={errores.titulo ? true : undefined}
                aria-describedby={errores.titulo ? 'error-titulo' : undefined}
              />
              {errores.titulo ? (
                <p className="error-campo" id="error-titulo">
                  {errores.titulo}
                </p>
              ) : null}
            </div>

            <div className="campos-dobles">
              <div className="campo">
                <label htmlFor="categoria">Categoría</label>
                <select
                  id="categoria"
                  required
                  value={idCategoria}
                  onChange={(evento) => setIdCategoria(Number(evento.target.value))}
                  aria-invalid={errores.idCategoria ? true : undefined}
                >
                  <option value={0} disabled>
                    Elige una categoría…
                  </option>
                  {catalogo.categorias.map((categoria) => (
                    <option key={categoria.id} value={categoria.id}>
                      {categoria.nombre}
                    </option>
                  ))}
                </select>
                {errores.idCategoria ? <p className="error-campo">{errores.idCategoria}</p> : null}
              </div>
              <div className="campo">
                <label htmlFor="prioridad">Prioridad</label>
                <select
                  id="prioridad"
                  required
                  value={idPrioridad}
                  onChange={(evento) => setIdPrioridad(Number(evento.target.value))}
                  aria-invalid={errores.idPrioridad ? true : undefined}
                >
                  <option value={0} disabled>
                    Elige una prioridad…
                  </option>
                  {catalogo.prioridades.map((prioridad) => (
                    <option key={prioridad.id} value={prioridad.id}>
                      {prioridad.nombre} · respuesta en {prioridad.slaHoras} h
                    </option>
                  ))}
                </select>
                {errores.idPrioridad ? <p className="error-campo">{errores.idPrioridad}</p> : null}
              </div>
            </div>

            <div className="campo">
              <label htmlFor="descripcion">Descripción</label>
              <textarea
                id="descripcion"
                required
                rows={7}
                maxLength={5000}
                placeholder="Qué hacías, qué mensaje aparece, desde cuándo ocurre…"
                value={descripcion}
                onChange={(evento) => setDescripcion(evento.target.value)}
                aria-invalid={errores.descripcion ? true : undefined}
                aria-describedby={errores.descripcion ? 'error-descripcion' : undefined}
              />
              {errores.descripcion ? (
                <p className="error-campo" id="error-descripcion">
                  {errores.descripcion}
                </p>
              ) : null}
            </div>

            <div className="fila">
              <button type="submit" className="boton" disabled={crear.isPending}>
                {crear.isPending ? 'Abriendo el ticket…' : 'Abrir ticket'}
              </button>
              <Link to="/mis-tickets" className="boton boton--secundario">
                Cancelar
              </Link>
            </div>
          </form>
        ) : null}
      </section>

      <aside aria-labelledby="titulo-sugerencias">
        <div className="tarjeta tarjeta--sugerencias">
          <h2 id="titulo-sugerencias">¿Quizá ya tiene respuesta?</h2>
          {!hayBusqueda ? (
            <p className="sutil">
              Escribe un título descriptivo y aquí aparecerán artículos del centro de ayuda
              relacionados.
            </p>
          ) : buscandoSugerencias && !sugerencias ? (
            <Cargando etiqueta="Buscando…" />
          ) : sugerencias && sugerencias.length > 0 ? (
            <ul className="lista-sugerencias">
              {sugerencias.slice(0, 4).map((articulo) => (
                <li key={articulo.id}>
                  <a
                    href={`/ayuda/${articulo.slug}`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {articulo.titulo}
                    <small>Se abre en una pestaña nueva</small>
                  </a>
                </li>
              ))}
            </ul>
          ) : (
            <p className="sutil">
              Sin artículos parecidos. Sigue con el formulario y el equipo de soporte lo mirará.
            </p>
          )}
        </div>
      </aside>
    </div>
  )
}
