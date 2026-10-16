/**
 * Tipos del contrato JSON de la API de Mesa 2.0.
 * Espejo de los DTOs de `backend/src/main/java/com/royaltown/mesa/dto`.
 * Las fechas llegan como ISO-8601 sin zona (LocalDateTime): "2026-10-16T09:30:00".
 */

export interface Usuario {
  id: number
  nombre: string
  email: string
  rol: string
  departamento: string | null
}

export interface Sla {
  relevante: boolean
  incumplido: boolean
  minutosRestantes: number | null
}

export interface TicketResumen {
  id: number
  titulo: string
  estado: string
  esFinal: boolean
  prioridad: string
  slaHoras: number
  categoria: string
  creador: string
  tecnico: string | null
  fechaCreacion: string
  fechaLimite: string
  fechaCierre: string | null
  sla: Sla
}

export interface Comentario {
  id: number
  autor: string
  interno: boolean
  texto: string
  fecha: string
}

export interface CambioHistorial {
  campo: string
  anterior: string | null
  nuevo: string | null
  autor: string
  fecha: string
}

export interface Valoracion {
  puntuacion: number
  comentario: string | null
  fecha: string
}

export interface TicketDetalle extends TicketResumen {
  descripcion: string
  idCategoria: number
  idPrioridad: number
  idCreador: number
  idTecnico: number | null
  comentarios: Comentario[]
  historial: CambioHistorial[]
  valoracion: Valoracion | null
}

export interface PaginaTickets {
  total: number
  pagina: number
  tamano: number
  elementos: TicketResumen[]
}

export interface EstadoCatalogo {
  id: number
  nombre: string
  orden: number
  esFinal: boolean
}

export interface PrioridadCatalogo {
  id: number
  nombre: string
  slaHoras: number
}

export interface CategoriaCatalogo {
  id: number
  nombre: string
  descripcion: string
}

export interface Transicion {
  origen: number
  destino: number
}

export interface Catalogo {
  estados: EstadoCatalogo[]
  prioridades: PrioridadCatalogo[]
  categorias: CategoriaCatalogo[]
  transiciones: Transicion[]
}

export interface ArticuloResumen {
  id: number
  slug: string
  titulo: string
  resumen: string
  categoria: string
  visitas: number
  votosUtil: number
  votosNoUtil: number
  publicado: boolean
}

export interface ArticuloDetalle {
  id: number
  slug: string
  titulo: string
  resumen: string
  contenido: string
  categoria: string
  idCategoria: number
  publicado: boolean
  visitas: number
  votosUtil: number
  votosNoUtil: number
  fechaCreacion: string
}

export interface Feedback {
  votosUtil: number
  votosNoUtil: number
}

export interface EstadoTotal {
  estado: string
  total: number
}

export interface TecnicoRendimiento {
  tecnico: string
  resueltos: number
  minutosMedios: number | null
}

export interface Kpis {
  abiertos: number
  slaIncumplidos: number
  porEstado: EstadoTotal[]
  porTecnico: TecnicoRendimiento[]
}

export interface Staff {
  id: number
  nombre: string
  rol: string
}

export interface FiltrosTickets {
  estado?: number
  prioridad?: number
  categoria?: number
  texto?: string
  mios?: boolean
  sinAsignar?: boolean
  pagina?: number
  tamano?: number
}

export interface PeticionTicket {
  titulo: string
  descripcion: string
  idCategoria: number
  idPrioridad: number
}

export interface PeticionArticulo {
  titulo: string
  slug: string
  resumen: string
  contenido: string
  idCategoria: number
  publicado: boolean
}
