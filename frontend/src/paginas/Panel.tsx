import { useKpis } from '../api/consultas'
import { mensajeDe } from '../api/cliente'
import { Aviso, Cargando, Vacio } from '../componentes/Avisos'
import { formatearDuracion } from '../utils/fechas'
import { nombreEstado } from '../utils/presentacion'

export function Panel() {
  const { data: kpis, isPending, isError, error } = useKpis()

  if (isPending) return <Cargando etiqueta="Calculando indicadores…" />
  if (isError || !kpis) {
    return (
      <div className="pila">
        <Aviso>{mensajeDe(error)}</Aviso>
      </div>
    )
  }

  const maximoEstado = Math.max(...kpis.porEstado.map((fila) => fila.total), 1)

  return (
    <div className="pila">
      <div>
        <h1>Panel de indicadores</h1>
        <p className="sutil">
          Métricas en vivo calculadas por las vistas SQL del esquema, las mismas que usa el CLI de
          ticketing.
        </p>
      </div>

      <div className="rejilla rejilla--kpi">
        <section className="tarjeta kpi">
          <h2>Abiertos</h2>
          <p className="kpi__valor">{kpis.abiertos}</p>
          <p className="sutil">tickets sin cerrar (Resuelto incluido)</p>
        </section>
        <section className="tarjeta kpi">
          <h2>SLA incumplidos</h2>
          <p className={`kpi__valor ${kpis.slaIncumplidos > 0 ? 'kpi__valor--alerta' : ''}`}>
            {kpis.slaIncumplidos}
          </p>
          <p className="sutil">tickets abiertos con la fecha límite pasada</p>
        </section>
      </div>

      <section className="tarjeta" aria-labelledby="titulo-por-estado">
        <h2 id="titulo-por-estado">Reparto por estado</h2>
        <ul className="barras">
          {kpis.porEstado.map((fila) => (
            <li key={fila.estado}>
              <span className="barras__etiqueta">{nombreEstado(fila.estado)}</span>
              <span className="barras__pista">
                <span
                  className="barras__relleno"
                  style={{ width: `${Math.round((fila.total / maximoEstado) * 100)}%` }}
                />
              </span>
              <span className="barras__valor">{fila.total}</span>
            </li>
          ))}
        </ul>
      </section>

      <section className="tarjeta" aria-labelledby="titulo-por-tecnico">
        <h2 id="titulo-por-tecnico">Resolución por técnico</h2>
        {kpis.porTecnico.length === 0 ? (
          <Vacio titulo="Todavía no hay cierres que promediar" />
        ) : (
          <div className="tabla-envoltura">
            <table className="tabla">
              <caption className="oculto-visual">Tickets resueltos y tiempo medio por técnico</caption>
              <thead>
                <tr>
                  <th scope="col">Técnico</th>
                  <th scope="col">Tickets resueltos</th>
                  <th scope="col">Tiempo medio hasta resolver</th>
                </tr>
              </thead>
              <tbody>
                {kpis.porTecnico.map((fila) => (
                  <tr key={fila.tecnico}>
                    <td>{fila.tecnico}</td>
                    <td>{fila.resueltos}</td>
                    <td>{formatearDuracion(fila.minutosMedios)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
