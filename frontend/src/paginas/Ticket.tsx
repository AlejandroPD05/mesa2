import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  useAsignarTicket,
  useCatalogo,
  useComentar,
  useEquipo,
  useParchearTicket,
  useTicket,
  useTransicionar,
  useValorarTicket,
} from '../api/consultas'
import { mensajeDe } from '../api/cliente'
import { useSesion } from '../contexto/contextoSesion'
import { Aviso, Cargando, Vacio } from '../componentes/Avisos'
import { Insignia, InsigniaEstado, InsigniaPrioridad, InsigniaSla } from '../componentes/Etiquetas'
import { formatearFechaHora, formatearRelativo } from '../utils/fechas'
import { nombreCampo, nombreEstado, nombreRol, valorLegible } from '../utils/presentacion'
import type { CambioHistorial, Catalogo, EstadoCatalogo, TicketDetalle, Valoracion } from '../api/tipos'

const ETIQUETAS_PUNTUACION = ['', 'Muy mal', 'Mal', 'Normal', 'Bien', 'Excelente']

const ETIQUETAS_TRANSICION: Record<string, string> = {
  En_Progreso: 'Ponerse a ello',
  Pendiente_Usuario: 'Pedir más información',
  Abierto: 'Devolver a la cola',
  Resuelto: 'Marcar como resuelto',
  Cerrado: 'Cerrar',
}

export function Ticket() {
  const { id } = useParams()
  const numeroId = Number(id)
  const { usuario } = useSesion()
  const { data: ticket, isPending, isError, error } = useTicket(numeroId)
  const { data: catalogo } = useCatalogo()

  const esStaff = usuario?.rol === 'tecnico' || usuario?.rol === 'administrador'

  if (!Number.isFinite(numeroId)) {
    return (
      <div className="pila">
        <Aviso>El ticket indicado no existe.</Aviso>
      </div>
    )
  }
  if (isPending) return <Cargando etiqueta="Cargando el ticket…" />
  if (isError || !ticket) {
    return (
      <div className="pila">
        <Aviso>{mensajeDe(error)}</Aviso>
        <p className="sutil">
          <Link to={esStaff ? '/bandeja' : '/mis-tickets'}>&larr; Volver</Link>
        </p>
      </div>
    )
  }

  const esCreador = ticket.idCreador === usuario?.id

  const estadoActual = catalogo?.estados.find((estado) => estado.nombre === ticket.estado)
  const destinos: EstadoCatalogo[] =
    catalogo && estadoActual
      ? catalogo.transiciones
          .filter((transicion) => transicion.origen === estadoActual.id)
          .map((transicion) => catalogo.estados.find((estado) => estado.id === transicion.destino))
          .filter((estado): estado is EstadoCatalogo => estado !== undefined)
      : []

  const claveAcciones = `t${ticket.id}-e${ticket.estado}-p${ticket.idPrioridad}-c${ticket.idCategoria}-k${ticket.idTecnico}`

  return (
    <div className="pila">
      <p className="sutil">
        <Link to={esStaff ? '/bandeja' : '/mis-tickets'}>&larr; Volver</Link> · Ticket #{ticket.id}
      </p>

      <header className="pila pila--fina">
        <div className="fila fila--separada">
          <h1>{ticket.titulo}</h1>
          <div className="fila">
            <InsigniaEstado estado={ticket.estado} />
            <InsigniaPrioridad prioridad={ticket.prioridad} />
            <InsigniaSla sla={ticket.sla} />
          </div>
        </div>
        <p className="sutil">
          Abierto por <strong>{ticket.creador}</strong> el {formatearFechaHora(ticket.fechaCreacion)} ·
          Categoría: {ticket.categoria}
        </p>
      </header>

      <div className="disposicion-ticket">
        <div className="pila">
          <section className="tarjeta" aria-labelledby="titulo-descripcion">
            <h2 id="titulo-descripcion">Descripción</h2>
            <p className="texto-con-saltos">{ticket.descripcion}</p>
          </section>

          <section className="pila" aria-labelledby="titulo-conversacion">
            <h2 id="titulo-conversacion">Conversación</h2>
            {ticket.comentarios.length === 0 ? (
              <Vacio titulo="Todavía no hay comentarios">
                Añade contexto o responde al equipo desde el formulario de abajo.
              </Vacio>
            ) : (
              <ul className="hilo">
                {ticket.comentarios.map((comentario) => (
                  <li
                    key={comentario.id}
                    className={comentario.interno ? 'comentario comentario--interno' : 'comentario'}
                  >
                    <div className="comentario__meta">
                      <strong>{comentario.autor}</strong>
                      {comentario.interno ? <Insignia tono="aviso">Nota interna</Insignia> : null}
                      <span className="sutil">
                        {formatearRelativo(comentario.fecha)} · {formatearFechaHora(comentario.fecha)}
                      </span>
                    </div>
                    <p className="texto-con-saltos">{comentario.texto}</p>
                  </li>
                ))}
              </ul>
            )}
            <FormularioComentario idTicket={ticket.id} esStaff={Boolean(esStaff)} />
          </section>
        </div>

        <aside className="pila">
          <Ficha ticket={ticket} />

          {esStaff && catalogo ? (
            <AccionesStaff key={claveAcciones} ticket={ticket} catalogo={catalogo} destinos={destinos} />
          ) : null}

          {!esStaff && esCreador ? (
            <AccionesCreador key={claveAcciones} ticket={ticket} destinos={destinos} />
          ) : null}

          {esStaff ? <Historial cambios={ticket.historial} /> : null}
        </aside>
      </div>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Conversación
// ---------------------------------------------------------------------------

function FormularioComentario({ idTicket, esStaff }: { idTicket: number; esStaff: boolean }) {
  const [texto, setTexto] = useState('')
  const [interno, setInterno] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const comentar = useComentar(idTicket)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (texto.trim().length === 0) {
      setError('Escribe un comentario antes de enviarlo.')
      return
    }
    setError(null)
    try {
      await comentar.mutateAsync({ texto: texto.trim(), interno })
      setTexto('')
      setInterno(false)
    } catch (fallo) {
      setError(mensajeDe(fallo))
    }
  }

  return (
    <form className="tarjeta formulario formulario--comentario" onSubmit={enviar}>
      <div className="campo">
        <label htmlFor="nuevo-comentario">Añadir comentario</label>
        <textarea
          id="nuevo-comentario"
          rows={3}
          maxLength={2000}
          placeholder="Añade contexto, responde al equipo o confirma la solución…"
          value={texto}
          onChange={(evento) => setTexto(evento.target.value)}
        />
      </div>
      {esStaff ? (
        <label className="casilla">
          <input
            type="checkbox"
            checked={interno}
            onChange={(evento) => setInterno(evento.target.checked)}
          />
          Nota interna (quien abrió el ticket no la verá)
        </label>
      ) : null}
      {error ? <Aviso>{error}</Aviso> : null}
      <div>
        <button type="submit" className="boton" disabled={comentar.isPending}>
          {comentar.isPending ? 'Enviando…' : 'Enviar comentario'}
        </button>
      </div>
    </form>
  )
}

// ---------------------------------------------------------------------------
// Ficha lateral
// ---------------------------------------------------------------------------

function Ficha({ ticket }: { ticket: TicketDetalle }) {
  return (
    <section className="tarjeta" aria-labelledby="titulo-ficha">
      <h2 id="titulo-ficha">Ficha</h2>
      <dl className="datos">
        <div>
          <dt>Estado</dt>
          <dd>
            <InsigniaEstado estado={ticket.estado} />
          </dd>
        </div>
        <div>
          <dt>Prioridad</dt>
          <dd>
            <InsigniaPrioridad prioridad={ticket.prioridad} />{' '}
            <span className="sutil">respuesta en {ticket.slaHoras} h</span>
          </dd>
        </div>
        <div>
          <dt>Fecha límite</dt>
          <dd>{formatearFechaHora(ticket.fechaLimite)}</dd>
        </div>
        {ticket.fechaCierre ? (
          <div>
            <dt>Fecha de cierre</dt>
            <dd>{formatearFechaHora(ticket.fechaCierre)}</dd>
          </div>
        ) : null}
        <div>
          <dt>Petición de</dt>
          <dd>{ticket.creador}</dd>
        </div>
        <div>
          <dt>Técnico</dt>
          <dd>{ticket.tecnico ?? <span className="sutil">Sin asignar</span>}</dd>
        </div>
        <div>
          <dt>Categoría</dt>
          <dd>{ticket.categoria}</dd>
        </div>
      </dl>
    </section>
  )
}

// ---------------------------------------------------------------------------
// Acciones del equipo de soporte
// ---------------------------------------------------------------------------

function AccionesStaff({
  ticket,
  catalogo,
  destinos,
}: {
  ticket: TicketDetalle
  catalogo: Catalogo
  destinos: EstadoCatalogo[]
}) {
  const transicionar = useTransicionar(ticket.id)
  const parchear = useParchearTicket(ticket.id)
  const asignar = useAsignarTicket(ticket.id)
  const { data: equipo } = useEquipo(true)

  const [resolviendo, setResolviendo] = useState(false)
  const [resolucion, setResolucion] = useState('')
  const [prioridadSel, setPrioridadSel] = useState(ticket.idPrioridad)
  const [categoriaSel, setCategoriaSel] = useState(ticket.idCategoria)
  const [tecnicoSel, setTecnicoSel] = useState(ticket.idTecnico != null ? String(ticket.idTecnico) : '')
  const [error, setError] = useState<string | null>(null)

  async function ejecutar(accion: () => Promise<unknown>) {
    setError(null)
    try {
      await accion()
    } catch (fallo) {
      setError(mensajeDe(fallo))
    }
  }

  const destinoResuelto = destinos.find((destino) => destino.nombre === 'Resuelto')
  const ocupado = transicionar.isPending || parchear.isPending || asignar.isPending
  const gestionCambiada = prioridadSel !== ticket.idPrioridad || categoriaSel !== ticket.idCategoria

  return (
    <section className="tarjeta" aria-labelledby="titulo-acciones">
      <h2 id="titulo-acciones">Gestión del ticket</h2>
      {error ? <Aviso>{error}</Aviso> : null}

      <h3>Estado</h3>
      {destinos.length === 0 ? (
        <p className="sutil">No hay transiciones disponibles desde este estado.</p>
      ) : (
        <ul className="acciones-lista">
          {destinos.map((destino) => (
            <li key={destino.id}>
              <button
                type="button"
                className={
                  destino.nombre === 'Resuelto'
                    ? 'boton boton--pequeno'
                    : 'boton boton--secundario boton--pequeno'
                }
                disabled={ocupado}
                onClick={() => {
                  if (destino.nombre === 'Resuelto') {
                    setResolviendo(true)
                    return
                  }
                  void ejecutar(() => transicionar.mutateAsync({ estadoDestino: destino.id }))
                }}
              >
                {ETIQUETAS_TRANSICION[destino.nombre] ?? `Pasar a ${nombreEstado(destino.nombre)}`}
              </button>
            </li>
          ))}
        </ul>
      )}

      {resolviendo && destinoResuelto ? (
        <div className="campo">
          <label htmlFor="resolucion">¿Cómo se ha resuelto?</label>
          <textarea
            id="resolucion"
            rows={4}
            maxLength={2000}
            placeholder="Cuéntale a quien abrió el ticket qué se ha hecho (mínimo 10 caracteres)."
            value={resolucion}
            onChange={(evento) => setResolucion(evento.target.value)}
          />
          <div className="fila">
            <button
              type="button"
              className="boton boton--pequeno"
              disabled={transicionar.isPending}
              onClick={() =>
                void ejecutar(async () => {
                  await transicionar.mutateAsync({
                    estadoDestino: destinoResuelto.id,
                    resolucion: resolucion.trim(),
                  })
                  setResolviendo(false)
                  setResolucion('')
                })
              }
            >
              Confirmar resolución
            </button>
            <button
              type="button"
              className="boton boton--fantasma boton--pequeno"
              onClick={() => setResolviendo(false)}
            >
              Cancelar
            </button>
          </div>
        </div>
      ) : null}

      <h3>Asignación</h3>
      <div className="campo">
        <label htmlFor="asignar-tecnico">Técnico responsable</label>
        <select
          id="asignar-tecnico"
          value={tecnicoSel}
          onChange={(evento) => setTecnicoSel(evento.target.value)}
        >
          <option value="">Sin asignar</option>
          {equipo?.map((miembro) => (
            <option key={miembro.id} value={miembro.id}>
              {miembro.nombre} ({nombreRol(miembro.rol)})
            </option>
          ))}
        </select>
        <div className="fila">
          <button
            type="button"
            className="boton boton--secundario boton--pequeno"
            disabled={ocupado || !tecnicoSel || tecnicoSel === String(ticket.idTecnico ?? '')}
            onClick={() => void ejecutar(() => asignar.mutateAsync(Number(tecnicoSel)))}
          >
            Asignar
          </button>
          {ticket.tecnico ? (
            <button
              type="button"
              className="boton boton--fantasma boton--pequeno"
              disabled={ocupado}
              onClick={() => void ejecutar(() => asignar.mutateAsync(null))}
            >
              Quitar asignación
            </button>
          ) : null}
        </div>
      </div>

      <h3>Clasificación</h3>
      <div className="campos-dobles">
        <div className="campo">
          <label htmlFor="cambiar-prioridad">Prioridad</label>
          <select
            id="cambiar-prioridad"
            value={prioridadSel}
            onChange={(evento) => setPrioridadSel(Number(evento.target.value))}
          >
            {catalogo.prioridades.map((prioridad) => (
              <option key={prioridad.id} value={prioridad.id}>
                {prioridad.nombre} · SLA {prioridad.slaHoras} h
              </option>
            ))}
          </select>
        </div>
        <div className="campo">
          <label htmlFor="cambiar-categoria">Categoría</label>
          <select
            id="cambiar-categoria"
            value={categoriaSel}
            onChange={(evento) => setCategoriaSel(Number(evento.target.value))}
          >
            {catalogo.categorias.map((categoria) => (
              <option key={categoria.id} value={categoria.id}>
                {categoria.nombre}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div>
        <button
          type="button"
          className="boton boton--secundario boton--pequeno"
          disabled={ocupado || !gestionCambiada}
          onClick={() => {
            const cambios: { idPrioridad?: number; idCategoria?: number } = {}
            if (prioridadSel !== ticket.idPrioridad) cambios.idPrioridad = prioridadSel
            if (categoriaSel !== ticket.idCategoria) cambios.idCategoria = categoriaSel
            void ejecutar(() => parchear.mutateAsync(cambios))
          }}
        >
          Aplicar cambios
        </button>
        <p className="ayuda-campo">
          Cambiar la prioridad recalcula la fecha límite del SLA (siempre desde la fecha de creación).
        </p>
      </div>
    </section>
  )
}

// ---------------------------------------------------------------------------
// Acciones de quien abrió el ticket
// ---------------------------------------------------------------------------

function AccionesCreador({ ticket, destinos }: { ticket: TicketDetalle; destinos: EstadoCatalogo[] }) {
  const transicionar = useTransicionar(ticket.id)
  const valorar = useValorarTicket(ticket.id)
  const [puntuacion, setPuntuacion] = useState(0)
  const [comentario, setComentario] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function ejecutar(accion: () => Promise<unknown>) {
    setError(null)
    try {
      await accion()
    } catch (fallo) {
      setError(mensajeDe(fallo))
    }
  }

  if (ticket.estado === 'Resuelto') {
    const cerrar = destinos.find((destino) => destino.nombre === 'Cerrado')
    const reabrir = destinos.find((destino) => destino.nombre === 'En_Progreso')
    if (!cerrar && !reabrir) return null
    return (
      <section className="tarjeta" aria-labelledby="titulo-confirmar">
        <h2 id="titulo-confirmar">¿Todo bien?</h2>
        <p className="sutil">
          El equipo ha marcado tu incidencia como resuelta. Confirma la solución o reábrela si sigue
          el problema.
        </p>
        {error ? <Aviso>{error}</Aviso> : null}
        <div className="fila">
          {cerrar ? (
            <button
              type="button"
              className="boton"
              disabled={transicionar.isPending}
              onClick={() => void ejecutar(() => transicionar.mutateAsync({ estadoDestino: cerrar.id }))}
            >
              Confirmar y cerrar
            </button>
          ) : null}
          {reabrir ? (
            <button
              type="button"
              className="boton boton--secundario"
              disabled={transicionar.isPending}
              onClick={() => void ejecutar(() => transicionar.mutateAsync({ estadoDestino: reabrir.id }))}
            >
              Sigue el problema: reabrir
            </button>
          ) : null}
        </div>
      </section>
    )
  }

  if (ticket.estado !== 'Cerrado') return null

  if (ticket.valoracion) {
    return <ValoracionMostrada valoracion={ticket.valoracion} />
  }

  return (
    <section className="tarjeta" aria-labelledby="titulo-valorar">
      <h2 id="titulo-valorar">Valora la atención</h2>
      {error ? <Aviso>{error}</Aviso> : null}
      <fieldset className="campo">
        <legend>Puntuación (de 1 a 5)</legend>
        <div className="puntuacion">
          {[1, 2, 3, 4, 5].map((valor) => (
            <label
              key={valor}
              className={puntuacion === valor ? 'puntuacion__opcion puntuacion__opcion--activa' : 'puntuacion__opcion'}
            >
              <input
                type="radio"
                name="puntuacion"
                value={valor}
                checked={puntuacion === valor}
                onChange={() => setPuntuacion(valor)}
              />
              {valor}
            </label>
          ))}
        </div>
        <p className="sutil" role="status">
          {puntuacion > 0 ? ETIQUETAS_PUNTUACION[puntuacion] : 'Elige una puntuación.'}
        </p>
      </fieldset>
      <div className="campo">
        <label htmlFor="comentario-valoracion">Comentario (opcional)</label>
        <textarea
          id="comentario-valoracion"
          rows={3}
          maxLength={255}
          value={comentario}
          onChange={(evento) => setComentario(evento.target.value)}
        />
      </div>
      <div>
        <button
          type="button"
          className="boton"
          disabled={valorar.isPending || puntuacion === 0}
          onClick={() =>
            void ejecutar(() =>
              valorar.mutateAsync({
                puntuacion,
                comentario: comentario.trim() === '' ? undefined : comentario.trim(),
              }),
            )
          }
        >
          {valorar.isPending ? 'Enviando…' : 'Enviar valoración'}
        </button>
      </div>
    </section>
  )
}

function ValoracionMostrada({ valoracion }: { valoracion: Valoracion }) {
  return (
    <section className="tarjeta" aria-labelledby="titulo-tu-valoracion">
      <h2 id="titulo-tu-valoracion">Tu valoración</h2>
      <p aria-label={`Puntuación: ${valoracion.puntuacion} de 5`}>
        <span aria-hidden="true" className="puntuacion-estrellas">
          {'★'.repeat(valoracion.puntuacion)}
          {'☆'.repeat(5 - valoracion.puntuacion)}
        </span>{' '}
        <span className="sutil">{ETIQUETAS_PUNTUACION[valoracion.puntuacion]}</span>
      </p>
      {valoracion.comentario ? <p>{valoracion.comentario}</p> : null}
      <p className="sutil">{formatearFechaHora(valoracion.fecha)}</p>
    </section>
  )
}

// ---------------------------------------------------------------------------
// Historial (solo equipo de soporte)
// ---------------------------------------------------------------------------

function Historial({ cambios }: { cambios: CambioHistorial[] }) {
  return (
    <section className="tarjeta" aria-labelledby="titulo-historial">
      <h2 id="titulo-historial">Historial</h2>
      <ol className="historial">
        {cambios.map((cambio, indice) => (
          <li key={indice}>
            <p>
              <strong>{nombreCampo(cambio.campo)}</strong>{' '}
              {cambio.anterior ? (
                <>
                  <del>{valorLegible(cambio.campo, cambio.anterior)}</del>{' '}
                  <span aria-hidden="true">→</span>{' '}
                </>
              ) : null}
              <ins>{valorLegible(cambio.campo, cambio.nuevo)}</ins>
            </p>
            <p className="sutil">
              {cambio.autor} · {formatearFechaHora(cambio.fecha)}
            </p>
          </li>
        ))}
      </ol>
    </section>
  )
}
