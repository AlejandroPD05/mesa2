import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router-dom'
import { useCatalogo, useTickets } from '../api/consultas'
import { mensajeDe } from '../api/cliente'
import { Aviso, Cargando, Vacio } from '../componentes/Avisos'
import { InsigniaEstado, InsigniaPrioridad, InsigniaSla } from '../componentes/Etiquetas'
import { Paginacion } from '../componentes/Paginacion'
import { formatearRelativo } from '../utils/fechas'

const TAMANO = 20

export function Bandeja() {
  const [estado, setEstado] = useState(0)
  const [prioridad, setPrioridad] = useState(0)
  const [sinAsignar, setSinAsignar] = useState(false)
  const [mios, setMios] = useState(false)
  const [texto, setTexto] = useState('')
  const textoDiferido = useDeferredValue(texto)
  const [pagina, setPagina] = useState(1)

  const { data: catalogo } = useCatalogo()
  const { data, isPending, isError, error, isFetching } = useTickets({
    estado: estado || undefined,
    prioridad: prioridad || undefined,
    sinAsignar: sinAsignar || undefined,
    mios: mios || undefined,
    texto: textoDiferido || undefined,
    pagina,
    tamano: TAMANO,
  })

  const hayFiltros = estado !== 0 || prioridad !== 0 || sinAsignar || mios || textoDiferido !== ''

  return (
    <div className="pila">
      <div>
        <h1>Bandeja de soporte</h1>
        <p className="sutil">
          Todo el tablero en un vistazo: filtra por estado, revisa lo que no tiene técnico y entra al
          detalle para actuar.
        </p>
      </div>

      <form className="filtros" role="search" onSubmit={(evento) => evento.preventDefault()}>
        <div className="campo">
          <label htmlFor="bandeja-texto">Buscar</label>
          <input
            id="bandeja-texto"
            type="search"
            placeholder="Título o descripción…"
            value={texto}
            onChange={(evento) => {
              setTexto(evento.target.value)
              setPagina(1)
            }}
          />
        </div>
        <div className="campo">
          <label htmlFor="bandeja-estado">Estado</label>
          <select
            id="bandeja-estado"
            value={estado}
            onChange={(evento) => {
              setEstado(Number(evento.target.value))
              setPagina(1)
            }}
          >
            <option value={0}>Todos</option>
            {catalogo?.estados.map((opcion) => (
              <option key={opcion.id} value={opcion.id}>
                {opcion.nombre.replaceAll('_', ' ')}
              </option>
            ))}
          </select>
        </div>
        <div className="campo">
          <label htmlFor="bandeja-prioridad">Prioridad</label>
          <select
            id="bandeja-prioridad"
            value={prioridad}
            onChange={(evento) => {
              setPrioridad(Number(evento.target.value))
              setPagina(1)
            }}
          >
            <option value={0}>Todas</option>
            {catalogo?.prioridades.map((opcion) => (
              <option key={opcion.id} value={opcion.id}>
                {opcion.nombre}
              </option>
            ))}
          </select>
        </div>
        <div className="campo campo--casillas">
          <label className="casilla">
            <input
              type="checkbox"
              checked={sinAsignar}
              onChange={(evento) => {
                setSinAsignar(evento.target.checked)
                setPagina(1)
              }}
            />
            Sin asignar
          </label>
          <label className="casilla">
            <input
              type="checkbox"
              checked={mios}
              onChange={(evento) => {
                setMios(evento.target.checked)
                setPagina(1)
              }}
            />
            Asignados a mí
          </label>
        </div>
      </form>

      {isError ? <Aviso>{mensajeDe(error)}</Aviso> : null}
      {isPending ? <Cargando etiqueta="Cargando la bandeja…" /> : null}

      {data && data.total === 0 ? (
        <Vacio titulo="Nada por aquí">
          {hayFiltros
            ? 'Ningún ticket encaja con los filtros actuales.'
            : 'La bandeja está vacía: no hay tickets que mostrar.'}
        </Vacio>
      ) : null}

      {data && data.total > 0 ? (
        <div className="tabla-envoltura" aria-busy={isFetching}>
          <table className="tabla">
            <caption className="oculto-visual">Bandeja de tickets del equipo de soporte</caption>
            <thead>
              <tr>
                <th scope="col">Ticket</th>
                <th scope="col">Estado</th>
                <th scope="col">Prioridad</th>
                <th scope="col">Plazo</th>
                <th scope="col">Técnico</th>
                <th scope="col">Petición de</th>
                <th scope="col">Creado</th>
              </tr>
            </thead>
            <tbody>
              {data.elementos.map((ticket) => (
                <tr key={ticket.id}>
                  <td>
                    <Link to={`/tickets/${ticket.id}`} className="enlace-ticket">
                      #{ticket.id} · {ticket.titulo}
                    </Link>
                  </td>
                  <td>
                    <InsigniaEstado estado={ticket.estado} />
                  </td>
                  <td>
                    <InsigniaPrioridad prioridad={ticket.prioridad} />
                  </td>
                  <td>
                    <InsigniaSla sla={ticket.sla} />
                    {!ticket.sla.relevante ? <span className="sutil">—</span> : null}
                  </td>
                  <td>
                    {ticket.tecnico ?? <span className="sutil">Sin asignar</span>}
                  </td>
                  <td>{ticket.creador}</td>
                  <td>{formatearRelativo(ticket.fechaCreacion)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      {data ? (
        <Paginacion pagina={data.pagina} total={data.total} tamano={data.tamano} onCambiar={setPagina} />
      ) : null}
    </div>
  )
}
