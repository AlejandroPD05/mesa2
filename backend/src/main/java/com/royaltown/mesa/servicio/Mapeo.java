package com.royaltown.mesa.servicio;

import com.royaltown.mesa.dto.AuthDtos;
import com.royaltown.mesa.dto.KbDtos;
import com.royaltown.mesa.dto.TicketDtos;
import com.royaltown.mesa.modelo.ArticuloKb;
import com.royaltown.mesa.modelo.Comentario;
import com.royaltown.mesa.modelo.HistorialCambio;
import com.royaltown.mesa.modelo.Ticket;
import com.royaltown.mesa.modelo.Usuario;
import com.royaltown.mesa.modelo.Valoracion;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Conversión de entidades a DTOs, en un único sitio. */
public final class Mapeo {

    private Mapeo() {
    }

    public static AuthDtos.UsuarioDto usuario(Usuario u) {
        return new AuthDtos.UsuarioDto(u.getId(), u.getNombreCompleto(), u.getEmail(),
                u.getRol().getNombre(), u.getDepartamento());
    }

    /**
     * El SLA deja de correr en cuanto el ticket tiene fecha de cierre (Resuelto
     * o Cerrado); mientras tanto, los minutos restantes pueden ser negativos.
     */
    public static TicketDtos.SlaDto sla(Ticket t, LocalDateTime ahora) {
        boolean relevante = t.getFechaCierre() == null;
        Long minutos = Duration.between(ahora, t.getFechaLimite()).toMinutes();
        return new TicketDtos.SlaDto(relevante, relevante && minutos < 0, minutos);
    }

    public static TicketDtos.TicketResumen resumen(Ticket t, LocalDateTime ahora) {
        return new TicketDtos.TicketResumen(
                t.getId(), t.getTitulo(),
                t.getEstado().getNombre(), t.getEstado().isEsFinal(),
                t.getPrioridad().getNombre(), t.getPrioridad().getSlaHoras(),
                t.getCategoria().getNombre(),
                t.getCreador().getNombreCompleto(),
                t.getTecnico() == null ? null : t.getTecnico().getNombreCompleto(),
                t.getFechaCreacion(), t.getFechaLimite(), t.getFechaCierre(),
                sla(t, ahora));
    }

    public static TicketDtos.ComentarioDto comentario(Comentario c) {
        return new TicketDtos.ComentarioDto(c.getId(), c.getAutor().getNombreCompleto(),
                c.isInterno(), c.getComentario(), c.getFechaComentario());
    }

    public static TicketDtos.HistorialDto historial(HistorialCambio h) {
        return new TicketDtos.HistorialDto(h.getCampo(), h.getValorAnterior(), h.getValorNuevo(),
                h.getAutor().getNombreCompleto(), h.getFechaCambio());
    }

    public static TicketDtos.ValoracionDto valoracion(Valoracion v) {
        return new TicketDtos.ValoracionDto(v.getPuntuacion(), v.getComentario(), v.getFechaValoracion());
    }

    public static TicketDtos.TicketDetalle detalle(Ticket t, List<Comentario> comentarios,
            List<HistorialCambio> historial, Valoracion valoracion, LocalDateTime ahora) {
        return new TicketDtos.TicketDetalle(
                t.getId(), t.getTitulo(), t.getDescripcion(),
                t.getEstado().getNombre(), t.getEstado().isEsFinal(),
                t.getPrioridad().getNombre(), t.getPrioridad().getSlaHoras(),
                t.getCategoria().getNombre(), t.getCategoria().getId(),
                t.getPrioridad().getId(),
                t.getCreador().getNombreCompleto(), t.getCreador().getId(),
                t.getTecnico() == null ? null : t.getTecnico().getNombreCompleto(),
                t.getTecnico() == null ? null : t.getTecnico().getId(),
                t.getFechaCreacion(), t.getFechaLimite(), t.getFechaCierre(),
                sla(t, ahora),
                comentarios.stream().map(Mapeo::comentario).toList(),
                historial.stream().map(Mapeo::historial).toList(),
                valoracion == null ? null : valoracion(valoracion));
    }

    public static KbDtos.ArticuloResumenDto articuloResumen(ArticuloKb a) {
        return new KbDtos.ArticuloResumenDto(a.getId(), a.getSlug(), a.getTitulo(), a.getResumen(),
                a.getCategoria().getNombre(), a.getVisitas(), a.getVotosUtil(), a.getVotosNoUtil(),
                a.isPublicado());
    }

    public static KbDtos.ArticuloDetalleDto articuloDetalle(ArticuloKb a) {
        return new KbDtos.ArticuloDetalleDto(a.getId(), a.getSlug(), a.getTitulo(), a.getResumen(),
                a.getContenido(), a.getCategoria().getNombre(), a.getCategoria().getId(),
                a.isPublicado(), a.getVisitas(), a.getVotosUtil(), a.getVotosNoUtil(),
                a.getFechaCreacion());
    }
}
