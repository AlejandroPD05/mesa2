import type { ReactNode } from 'react'
import type { Sla } from '../api/tipos'
import { comoTiempoRestante } from '../utils/fechas'
import { nombreEstado, tonoEstado, tonoPrioridad, type Tono } from '../utils/presentacion'

export function Insignia({ tono, children }: { tono: Tono; children: ReactNode }) {
  return <span className={`insignia insignia--${tono}`}>{children}</span>
}

export function InsigniaEstado({ estado }: { estado: string }) {
  return <Insignia tono={tonoEstado(estado)}>{nombreEstado(estado)}</Insignia>
}

export function InsigniaPrioridad({ prioridad }: { prioridad: string }) {
  return <Insignia tono={tonoPrioridad(prioridad)}>{prioridad}</Insignia>
}

/**
 * Chip del SLA: mientras el ticket está vivo muestra cuánto queda; si la
 * fecha límite ya pasó, avisa en rojo con el retraso acumulado. Si el ticket
 * ya cerró, el SLA dejó de ser relevante y no se pinta nada.
 */
export function InsigniaSla({ sla }: { sla: Sla }) {
  if (!sla.relevante) return null
  if (sla.incumplido) {
    const retraso = sla.minutosRestantes != null ? comoTiempoRestante(sla.minutosRestantes) : ''
    return <Insignia tono="peligro">SLA incumplido{retraso ? ` · ${retraso} de retraso` : ''}</Insignia>
  }
  const restante = comoTiempoRestante(sla.minutosRestantes)
  const tono: Tono = (sla.minutosRestantes ?? 0) < 60 ? 'aviso' : 'neutro'
  return <Insignia tono={tono}>SLA: queda{restante ? ` ${restante}` : ''}</Insignia>
}
