/** Etiquetas legibles (en español) y tonos visuales para estados y prioridades. */

export type Tono = 'neutro' | 'info' | 'progreso' | 'aviso' | 'exito' | 'peligro'

const NOMBRES_ESTADO: Record<string, string> = {
  Abierto: 'Abierto',
  En_Progreso: 'En progreso',
  Pendiente_Usuario: 'Pendiente de ti',
  Resuelto: 'Resuelto',
  Cerrado: 'Cerrado',
}

export function nombreEstado(nombre: string): string {
  return NOMBRES_ESTADO[nombre] ?? nombre.replaceAll('_', ' ')
}

const TONOS_ESTADO: Record<string, Tono> = {
  Abierto: 'info',
  En_Progreso: 'progreso',
  Pendiente_Usuario: 'aviso',
  Resuelto: 'exito',
  Cerrado: 'neutro',
}

export function tonoEstado(nombre: string): Tono {
  return TONOS_ESTADO[nombre] ?? 'neutro'
}

const TONOS_PRIORIDAD: Record<string, Tono> = {
  Baja: 'neutro',
  Media: 'info',
  Alta: 'aviso',
  Critica: 'peligro',
}

export function tonoPrioridad(nombre: string): Tono {
  return TONOS_PRIORIDAD[nombre] ?? 'neutro'
}

const NOMBRES_ROL: Record<string, string> = {
  empleado: 'Empleado',
  tecnico: 'Técnico',
  administrador: 'Administrador',
}

export function nombreRol(rol: string): string {
  return NOMBRES_ROL[rol] ?? rol
}

const NOMBRES_CAMPO: Record<string, string> = {
  estado: 'Estado',
  prioridad: 'Prioridad',
  categoria: 'Categoría',
  tecnico_asignado: 'Técnico asignado',
}

export function nombreCampo(campo: string): string {
  return NOMBRES_CAMPO[campo] ?? campo.replaceAll('_', ' ')
}

/** En el historial, los valores de «estado» se muestran con su nombre legible. */
export function valorLegible(campo: string, valor: string | null): string {
  if (valor == null) return '—'
  return campo === 'estado' ? nombreEstado(valor) : valor
}

/** A dónde llevar a cada perfil justo después de iniciar sesión. */
export function destinoPorRol(rol: string): string {
  return rol === 'empleado' ? '/mis-tickets' : '/bandeja'
}
