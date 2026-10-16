/**
 * Hooks de datos (TanStack Query) sobre la API de Mesa 2.0. Las mutaciones
 * refrescan el detalle afectado y las listas para que la interfaz se quede
 * coherente sin recargar la página.
 */
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { enviar, obtener } from './cliente'
import type {
  ArticuloDetalle,
  ArticuloResumen,
  Catalogo,
  Feedback,
  FiltrosTickets,
  Kpis,
  PaginaTickets,
  PeticionArticulo,
  PeticionTicket,
  Staff,
  TicketDetalle,
} from './tipos'

function cadenaDe(filtros: Record<string, string | number | boolean | undefined>): string {
  const parametros = new URLSearchParams()
  for (const [clave, valor] of Object.entries(filtros)) {
    if (valor !== undefined && valor !== '' && valor !== false) {
      parametros.set(clave, String(valor))
    }
  }
  return parametros.toString()
}

// ---------------------------------------------------------------------------
// Sesión y catálogos
// ---------------------------------------------------------------------------

export function useCatalogo() {
  return useQuery({
    queryKey: ['catalogo'],
    queryFn: () => obtener<Catalogo>('/api/catalogo'),
    staleTime: 5 * 60_000,
  })
}

export function useEquipo(habilitado: boolean) {
  return useQuery({
    queryKey: ['equipo'],
    queryFn: () => obtener<Staff[]>('/api/usuarios/tecnicos'),
    enabled: habilitado,
    staleTime: 5 * 60_000,
  })
}

// ---------------------------------------------------------------------------
// Tickets
// ---------------------------------------------------------------------------

export function useTickets(filtros: FiltrosTickets) {
  const cadena = cadenaDe({ ...filtros })
  return useQuery({
    queryKey: ['tickets', cadena],
    queryFn: () => obtener<PaginaTickets>(`/api/tickets?${cadena}`),
    placeholderData: keepPreviousData,
  })
}

export function useTicket(id: number) {
  return useQuery({
    queryKey: ['ticket', id],
    queryFn: () => obtener<TicketDetalle>(`/api/tickets/${id}`),
    enabled: Number.isFinite(id),
  })
}

export function useCrearTicket() {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (peticion: PeticionTicket) => enviar<TicketDetalle>('/api/tickets', 'POST', peticion),
    onSuccess: (ticket) => {
      cliente.setQueryData(['ticket', ticket.id], ticket)
      void cliente.invalidateQueries({ queryKey: ['tickets'] })
    },
  })
}

export function useComentar(id: number) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (peticion: { texto: string; interno: boolean }) =>
      enviar<TicketDetalle>(`/api/tickets/${id}/comentarios`, 'POST', peticion),
    onSuccess: (ticket) => {
      cliente.setQueryData(['ticket', id], ticket)
      void cliente.invalidateQueries({ queryKey: ['tickets'] })
    },
  })
}

export function useTransicionar(id: number) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (peticion: { estadoDestino: number; resolucion?: string }) =>
      enviar<TicketDetalle>(`/api/tickets/${id}/transiciones`, 'POST', peticion),
    onSuccess: (ticket) => {
      cliente.setQueryData(['ticket', id], ticket)
      void cliente.invalidateQueries({ queryKey: ['tickets'] })
      void cliente.invalidateQueries({ queryKey: ['kpis'] })
    },
  })
}

export function useParchearTicket(id: number) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (peticion: { idPrioridad?: number; idCategoria?: number }) =>
      enviar<TicketDetalle>(`/api/tickets/${id}`, 'PATCH', peticion),
    onSuccess: (ticket) => {
      cliente.setQueryData(['ticket', id], ticket)
      void cliente.invalidateQueries({ queryKey: ['tickets'] })
    },
  })
}

export function useAsignarTicket(id: number) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (idTecnico: number | null) =>
      enviar<TicketDetalle>(`/api/tickets/${id}/asignacion`, 'POST', { idTecnico }),
    onSuccess: (ticket) => {
      cliente.setQueryData(['ticket', id], ticket)
      void cliente.invalidateQueries({ queryKey: ['tickets'] })
    },
  })
}

export function useValorarTicket(id: number) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (peticion: { puntuacion: number; comentario?: string }) =>
      enviar<TicketDetalle>(`/api/tickets/${id}/valoracion`, 'POST', peticion),
    onSuccess: (ticket) => {
      cliente.setQueryData(['ticket', id], ticket)
    },
  })
}

// ---------------------------------------------------------------------------
// Centro de ayuda
// ---------------------------------------------------------------------------

export function useArticulos(filtros: { texto?: string; categoria?: number; todos?: boolean; activo?: boolean }) {
  const cadena = cadenaDe({ texto: filtros.texto, categoria: filtros.categoria, todos: filtros.todos })
  return useQuery({
    queryKey: ['articulos', cadena],
    queryFn: () => obtener<ArticuloResumen[]>(`/api/kb/articulos?${cadena}`),
    placeholderData: keepPreviousData,
    enabled: filtros.activo ?? true,
  })
}

export function useArticulo(slug: string) {
  return useQuery({
    queryKey: ['articulo', slug],
    queryFn: () => obtener<ArticuloDetalle>(`/api/kb/articulos/${slug}`),
    enabled: slug.length > 0,
  })
}

export function useFeedback(slug: string, id: number) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (util: boolean) => enviar<Feedback>(`/api/kb/articulos/${id}/feedback`, 'POST', { util }),
    onSuccess: () => {
      void cliente.invalidateQueries({ queryKey: ['articulo', slug] })
      void cliente.invalidateQueries({ queryKey: ['articulos'] })
    },
  })
}

export function useGuardarArticulo(idExistente?: number) {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (peticion: PeticionArticulo) =>
      idExistente === undefined
        ? enviar<ArticuloDetalle>('/api/kb/articulos', 'POST', peticion)
        : enviar<ArticuloDetalle>(`/api/kb/articulos/${idExistente}`, 'PUT', peticion),
    onSuccess: (articulo) => {
      cliente.setQueryData(['articulo', articulo.slug], articulo)
      void cliente.invalidateQueries({ queryKey: ['articulos'] })
    },
  })
}

export function useEliminarArticulo() {
  const cliente = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => enviar<void>(`/api/kb/articulos/${id}`, 'DELETE'),
    onSuccess: () => {
      void cliente.invalidateQueries({ queryKey: ['articulos'] })
    },
  })
}

// ---------------------------------------------------------------------------
// Informes
// ---------------------------------------------------------------------------

export function useKpis() {
  return useQuery({
    queryKey: ['kpis'],
    queryFn: () => obtener<Kpis>('/api/informes/kpis'),
  })
}
