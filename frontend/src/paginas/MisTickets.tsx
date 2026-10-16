import { useDeferredValue, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useCatalogo, useTickets } from '../api/consultas'
import { mensajeDe } from '../api/cliente'
import { useSesion } from '../contexto/contextoSesion'
import { Aviso, Cargando, Vacio } from '../componentes/Avisos'
import { InsigniaEstado, InsigniaPrioridad, InsigniaSla } from '../componentes/Etiquetas'
import { Paginacion } from '../componentes/Paginacion'
import { formatearRelativo } from '../utils/fechas'

const TAMANO = 10

export function MisTickets() {
  const { usuario } = useSesion()
  const [estado, setEstado] = useState(0)
  const [prioridad, setPrioridad] = useState(0)
  const [texto, setTexto] = useState('')
  const textoDiferido = useDeferredValue(texto)
  const [pagina, setPagina] = useState(1)

  const { data: catalogo } = useCatalogo()
  const { data, isPending, isError, error, isFetching } = useTickets({
    estado: estado || undefined,
    prioridad: prioridad || undefined,
    texto: textoDiferido || undefined,
    pagina,
    tamano: TAMANO,
  })

  // «Mis tickets» es la vista del empleado; el equipo de soporte tiene la bandeja.
  if (usuario && usuario.rol !== 'empleado') {
    return <Navigate to="/bandeja" replace />
  }

  const sinTickets = data !== undefined && data.total === 0 && !textoDiferido && estado === 0 && prioridad === 0

  return (
    <div className="pila">
      <div className="fila fila--separada">
        <div>
          <h1>Mis tickets</h1>
          <p className="sutil">Abre incidencias, sigue su estado y confirma la solución cuando llegue.</p>
        </div>
        <Link to="/tickets/nuevo" className="boton">
          Nuevo ticket
        </Link>
      </div>

      <form className="filtros" role="search" onSubmit={(evento) => evento.preventDefault()}>
        <div className="campo">
          <label htmlFor="filtro-texto">Buscar</label>
          <input
            id="filtro-texto"
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
          <label htmlFor="filtro-estado">Estado</label>
          <select
            id="filtro-estado"
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
          <label htmlFor="filtro-prioridad">Prioridad</label>
          <select
            id="filtro-prioridad"
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
      </form>

      {isError ? <Aviso>{mensajeDe(error)}</Aviso> : null}
      {isPending ? <Cargando etiqueta="Cargando tus tickets…" /> : null}

      {sinTickets ? (
        <Vacio titulo="Aún no has abierto ningún ticket">
          Cuando algo te bloquee, <Link to="/tickets/nuevo">cuéntanoslo</Link> y lo pondremos en marcha.
        </Vacio>
      ) : null}

      {data && data.total > 0 ? (
        <div className="tabla-envoltura" aria-busy={isFetching}>
          <table className="tabla">
            <caption className="oculto-visual">Listado de tus tickets</caption>
            <thead>
              <tr>
                <th scope="col">Ticket</th>
                <th scope="col">Estado</th>
                <th scope="col">Prioridad</th>
                <th scope="col">Plazo</th>
                <th scope="col">Técnico</th>
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
                  <td>{ticket.tecnico ?? <span className="sutil">Sin asignar</span>}</td>
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
