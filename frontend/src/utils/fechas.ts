/** Formato de fechas y duraciones en español para todo el portal. */

const formateadorFechaHora = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

const formateadorFecha = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
})

export function formatearFechaHora(iso: string | null | undefined): string {
  if (!iso) return '—'
  return formateadorFechaHora.format(new Date(iso))
}

export function formatearFecha(iso: string | null | undefined): string {
  if (!iso) return '—'
  return formateadorFecha.format(new Date(iso))
}

/** «hace 5 min», «hace 3 h», «hace 2 días»… */
export function formatearRelativo(iso: string): string {
  const minutos = Math.round((Date.now() - new Date(iso).getTime()) / 60_000)
  if (minutos < 1) return 'ahora mismo'
  if (minutos < 60) return `hace ${minutos} min`
  const horas = Math.floor(minutos / 60)
  if (horas < 24) return `hace ${horas} h`
  const dias = Math.floor(horas / 24)
  if (dias < 30) return `hace ${dias} ${dias === 1 ? 'día' : 'días'}`
  return formatearFecha(iso)
}

/** Duración en formato «2 h 15 min». */
export function formatearDuracion(minutos: number | null | undefined): string {
  if (minutos == null) return '—'
  const total = Math.abs(Math.round(minutos))
  const horas = Math.floor(total / 60)
  const resto = total % 60
  if (horas === 0) return `${resto} min`
  if (resto === 0) return `${horas} h`
  return `${horas} h ${resto} min`
}

/** Tiempo restante del SLA: admite valores de varios días («3 días 4 h»). */
export function comoTiempoRestante(minutos: number | null | undefined): string {
  if (minutos == null) return ''
  const total = Math.abs(Math.round(minutos))
  const horas = Math.floor(total / 60)
  const resto = total % 60
  if (horas >= 48) {
    const dias = Math.floor(horas / 24)
    const horasSueltas = horas % 24
    return horasSueltas === 0 ? `${dias} días` : `${dias} días ${horasSueltas} h`
  }
  if (horas >= 1) return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`
  return `${resto} min`
}
