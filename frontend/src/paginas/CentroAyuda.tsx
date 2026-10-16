import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router-dom'
import { useArticulos } from '../api/consultas'
import { mensajeDe } from '../api/cliente'
import { useSesion } from '../contexto/contextoSesion'
import { Aviso, Cargando, Vacio } from '../componentes/Avisos'

export function CentroAyuda() {
  const [busqueda, setBusqueda] = useState('')
  const busquedaDiferida = useDeferredValue(busqueda)
  const { usuario } = useSesion()
  const { data: articulos, isPending, isError, error } = useArticulos({ texto: busquedaDiferida })

  const sinResultados = articulos !== undefined && articulos.length === 0

  return (
    <div className="pila">
      <section className="heroe">
        <h1>Centro de ayuda</h1>
        <p className="heroe__entradilla">
          Respuestas rápidas del equipo de soporte de RoyalTown. Busca aquí antes de abrir un ticket:
          puede que tu duda ya tenga solución.
        </p>
        <div className="buscador">
          <label className="oculto-visual" htmlFor="busqueda-kb">
            Buscar en el centro de ayuda
          </label>
          <input
            id="busqueda-kb"
            type="search"
            placeholder="Ej.: impresora, correo, VPN…"
            value={busqueda}
            onChange={(evento) => setBusqueda(evento.target.value)}
          />
        </div>
      </section>

      {isError ? <Aviso>{mensajeDe(error)}</Aviso> : null}

      {isPending ? <Cargando etiqueta="Buscando artículos…" /> : null}

      {sinResultados ? (
        <Vacio titulo={`Sin resultados para «${busquedaDiferida}»`}>
          {usuario ? (
            <>
              Prueba con otras palabras o <Link to="/tickets/nuevo">abre un ticket</Link>: el equipo de
              soporte lo atenderá.
            </>
          ) : (
            <>
              Prueba con otras palabras o <Link to="/login">inicia sesión</Link> para abrir un ticket.
            </>
          )}
        </Vacio>
      ) : null}

      {articulos && articulos.length > 0 ? (
        <section aria-label="Artículos del centro de ayuda">
          <p className="sutil">
            {articulos.length} {articulos.length === 1 ? 'artículo' : 'artículos'}
          </p>
          <ul className="rejilla rejilla--articulos">
            {articulos.map((articulo) => (
              <li key={articulo.id}>
                <Link to={`/ayuda/${articulo.slug}`} className="tarjeta tarjeta--enlace">
                  <span className="tarjeta__meta">{articulo.categoria}</span>
                  <h2 className="tarjeta__titulo">{articulo.titulo}</h2>
                  <p className="tarjeta__texto">{articulo.resumen}</p>
                  <span className="tarjeta__pie">
                    {articulo.visitas} {articulo.visitas === 1 ? 'visita' : 'visitas'} ·{' '}
                    {articulo.votosUtil} {articulo.votosUtil === 1 ? 'voto útil' : 'votos útiles'}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      ) : null}

      <section className="banda">
        {usuario ? (
          <>
            <strong>¿No encuentras la solución?</strong> <Link to="/tickets/nuevo">Abre un ticket</Link> y
            sigue su estado desde tu buzón.
          </>
        ) : (
          <>
            <strong>¿Necesitas soporte personal?</strong> <Link to="/login">Inicia sesión</Link> o{' '}
            <Link to="/registro">crea tu cuenta</Link> para abrir un ticket.
          </>
        )}
      </section>
    </div>
  )
}
