package com.royaltown.mesa.servicio;

import com.royaltown.mesa.dto.TicketDtos.AsignacionPeticion;
import com.royaltown.mesa.dto.TicketDtos.ComentarioPeticion;
import com.royaltown.mesa.dto.TicketDtos.CrearTicketPeticion;
import com.royaltown.mesa.dto.TicketDtos.PaginaTickets;
import com.royaltown.mesa.dto.TicketDtos.PatchTicketPeticion;
import com.royaltown.mesa.dto.TicketDtos.TicketDetalle;
import com.royaltown.mesa.dto.TicketDtos.TicketResumen;
import com.royaltown.mesa.dto.TicketDtos.TransicionPeticion;
import com.royaltown.mesa.dto.TicketDtos.ValoracionPeticion;
import com.royaltown.mesa.error.NoEncontradoException;
import com.royaltown.mesa.error.ReglaNegocioException;
import com.royaltown.mesa.error.SinPermisoException;
import com.royaltown.mesa.modelo.Categoria;
import com.royaltown.mesa.modelo.Comentario;
import com.royaltown.mesa.modelo.Estado;
import com.royaltown.mesa.modelo.HistorialCambio;
import com.royaltown.mesa.modelo.Prioridad;
import com.royaltown.mesa.modelo.Ticket;
import com.royaltown.mesa.modelo.TransicionEstado;
import com.royaltown.mesa.modelo.Usuario;
import com.royaltown.mesa.modelo.Valoracion;
import com.royaltown.mesa.repo.CategoriaRepository;
import com.royaltown.mesa.repo.ComentarioRepository;
import com.royaltown.mesa.repo.EstadoRepository;
import com.royaltown.mesa.repo.HistorialRepository;
import com.royaltown.mesa.repo.PrioridadRepository;
import com.royaltown.mesa.repo.TicketRepository;
import com.royaltown.mesa.repo.TransicionRepository;
import com.royaltown.mesa.repo.UsuarioRepository;
import com.royaltown.mesa.repo.ValoracionRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketServicio {

    static final String ESTADO_RESUELTO = "Resuelto";
    static final String ESTADO_CERRADO = "Cerrado";
    static final String ESTADO_EN_PROGRESO = "En_Progreso";
    static final String ESTADO_ABIERTO = "Abierto";

    private final TicketRepository tickets;
    private final ComentarioRepository comentarios;
    private final HistorialRepository historial;
    private final ValoracionRepository valoraciones;
    private final EstadoRepository estados;
    private final PrioridadRepository prioridades;
    private final CategoriaRepository categorias;
    private final TransicionRepository transiciones;
    private final UsuarioRepository usuarios;
    private final SesionServicio sesionServicio;

    public TicketServicio(TicketRepository tickets, ComentarioRepository comentarios,
            HistorialRepository historial, ValoracionRepository valoraciones,
            EstadoRepository estados, PrioridadRepository prioridades,
            CategoriaRepository categorias, TransicionRepository transiciones,
            UsuarioRepository usuarios, SesionServicio sesionServicio) {
        this.tickets = tickets;
        this.comentarios = comentarios;
        this.historial = historial;
        this.valoraciones = valoraciones;
        this.estados = estados;
        this.prioridades = prioridades;
        this.categorias = categorias;
        this.transiciones = transiciones;
        this.usuarios = usuarios;
        this.sesionServicio = sesionServicio;
    }

    // ------------------------------------------------------------------
    // Listado
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PaginaTickets listar(Integer idEstado, Integer idPrioridad, Integer idCategoria,
            String texto, boolean soloMios, boolean sinAsignar, int pagina, int tamano) {
        Sesion sesion = sesionServicio.actual();
        int p = Math.max(pagina, 1);
        int t = Math.min(Math.max(tamano, 1), 100);

        String patron = (texto == null || texto.isBlank()) ? null : "%" + texto.trim().toLowerCase() + "%";
        Integer estadoFiltro = idEstado;
        Integer prioridadFiltro = idPrioridad;
        Integer categoriaFiltro = idCategoria;

        Specification<Ticket> especificacion = (root, query, cb) -> {
            // Los fetch solo en la consulta de datos, nunca en la de contar.
            if (Ticket.class.equals(query.getResultType())) {
                root.fetch("estado", JoinType.LEFT);
                root.fetch("prioridad", JoinType.LEFT);
                root.fetch("categoria", JoinType.LEFT);
                root.fetch("creador", JoinType.LEFT);
                root.fetch("tecnico", JoinType.LEFT);
            }
            List<Predicate> condiciones = new ArrayList<>();
            if (sesion.esStaff()) {
                if (soloMios) {
                    condiciones.add(cb.equal(root.get("tecnico").get("id"), sesion.id()));
                }
                if (sinAsignar) {
                    condiciones.add(cb.isNull(root.get("tecnico")));
                }
            } else {
                // Un empleado solo ve sus propios tickets.
                condiciones.add(cb.equal(root.get("creador").get("id"), sesion.id()));
            }
            if (estadoFiltro != null) {
                condiciones.add(cb.equal(root.get("estado").get("id"), estadoFiltro));
            }
            if (prioridadFiltro != null) {
                condiciones.add(cb.equal(root.get("prioridad").get("id"), prioridadFiltro));
            }
            if (categoriaFiltro != null) {
                condiciones.add(cb.equal(root.get("categoria").get("id"), categoriaFiltro));
            }
            if (patron != null) {
                condiciones.add(cb.or(
                        cb.like(cb.lower(root.get("titulo")), patron),
                        cb.like(cb.lower(root.get("descripcion")), patron)));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };

        Page<Ticket> paginaResultado = tickets.findAll(especificacion,
                PageRequest.of(p - 1, t, Sort.by(Sort.Direction.DESC, "fechaCreacion")));
        LocalDateTime ahora = LocalDateTime.now();
        List<TicketResumen> elementos = paginaResultado.getContent().stream()
                .map(ticket -> Mapeo.resumen(ticket, ahora))
                .toList();
        return new PaginaTickets(paginaResultado.getTotalElements(), p, t, elementos);
    }

    // ------------------------------------------------------------------
    // Detalle y creación
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public TicketDetalle detalle(Integer id) {
        Sesion sesion = sesionServicio.actual();
        return armarDetalle(cargarConAcceso(id, sesion), sesion);
    }

    @Transactional
    public TicketDetalle crear(CrearTicketPeticion peticion) {
        Sesion sesion = sesionServicio.actual();
        Usuario creador = usuarios.getReferenceById(sesion.id());
        Categoria categoria = categorias.findById(peticion.idCategoria())
                .orElseThrow(() -> new ReglaNegocioException("La categoría indicada no existe"));
        Prioridad prioridad = prioridades.findById(peticion.idPrioridad())
                .orElseThrow(() -> new ReglaNegocioException("La prioridad indicada no existe"));
        Estado abierto = estados.findByNombre(ESTADO_ABIERTO)
                .orElseThrow(() -> new IllegalStateException("Falta el estado «Abierto» en la base de datos"));

        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        Ticket ticket = new Ticket();
        ticket.setTitulo(peticion.titulo().trim());
        ticket.setDescripcion(peticion.descripcion().trim());
        ticket.setEstado(abierto);
        ticket.setPrioridad(prioridad);
        ticket.setCategoria(categoria);
        ticket.setCreador(creador);
        ticket.setFechaCreacion(ahora);
        // El SLA se fija al crear: fecha_limite = ahora + horas de la prioridad.
        ticket.setFechaLimite(ahora.plusHours(prioridad.getSlaHoras()));
        ticket = tickets.save(ticket);
        registrarHistorial(ticket, creador, "estado", null, abierto.getNombre());

        return armarDetalle(recargar(ticket.getId()), sesion);
    }

    // ------------------------------------------------------------------
    // Hilo de conversación
    // ------------------------------------------------------------------

    @Transactional
    public TicketDetalle comentar(Integer id, ComentarioPeticion peticion) {
        Sesion sesion = sesionServicio.actual();
        Ticket ticket = cargarConAcceso(id, sesion);
        if (peticion.interno() && !sesion.esStaff()) {
            throw new SinPermisoException("Solo el equipo de soporte puede escribir notas internas");
        }
        Comentario comentario = new Comentario();
        comentario.setTicket(ticket);
        comentario.setAutor(usuarios.getReferenceById(sesion.id()));
        comentario.setComentario(peticion.texto().trim());
        comentario.setInterno(peticion.interno());
        comentario.setFechaComentario(LocalDateTime.now().withNano(0));
        comentarios.save(comentario);
        return armarDetalle(ticket, sesion);
    }

    // ------------------------------------------------------------------
    // Ciclo de vida
    // ------------------------------------------------------------------

    @Transactional
    public TicketDetalle transicionar(Integer id, TransicionPeticion peticion) {
        Sesion sesion = sesionServicio.actual();
        Ticket ticket = cargarConAcceso(id, sesion);
        Estado origen = ticket.getEstado();
        Estado destino = estados.findById(peticion.estadoDestino())
                .orElseThrow(() -> new ReglaNegocioException("El estado indicado no existe"));

        List<TransicionEstado> matriz = transiciones.todas();
        boolean legal = matriz.stream().anyMatch(t -> t.getOrigen().getId().equals(origen.getId())
                && t.getDestino().getId().equals(destino.getId()));
        if (!legal) {
            List<String> validas = matriz.stream()
                    .filter(t -> t.getOrigen().getId().equals(origen.getId()))
                    .map(t -> t.getDestino().getNombre())
                    .toList();
            throw new ReglaNegocioException(
                    "Desde «" + origen.getNombre() + "» no se puede pasar a «" + destino.getNombre() + "»",
                    Map.of("estadoActual", origen.getNombre(), "transicionesValidas", validas));
        }

        if (!sesion.esStaff()) {
            boolean puedeConfirmar = ESTADO_RESUELTO.equals(origen.getNombre())
                    && (ESTADO_CERRADO.equals(destino.getNombre()) || ESTADO_EN_PROGRESO.equals(destino.getNombre()));
            if (!puedeConfirmar) {
                throw new SinPermisoException("Tu perfil solo puede confirmar la solución o reabrir un ticket resuelto");
            }
        }

        String resolucion = peticion.resolucion() == null ? null : peticion.resolucion().trim();
        if (ESTADO_RESUELTO.equals(destino.getNombre()) && (resolucion == null || resolucion.length() < 10)) {
            throw new ReglaNegocioException("Indica la solución aplicada (al menos 10 caracteres)");
        }

        Usuario autor = usuarios.getReferenceById(sesion.id());
        if (ESTADO_RESUELTO.equals(destino.getNombre()) || ESTADO_CERRADO.equals(destino.getNombre())) {
            if (ticket.getFechaCierre() == null) {
                ticket.setFechaCierre(LocalDateTime.now().withNano(0));
            }
        } else {
            // Reapertura: el SLA vuelve a estar vivo.
            ticket.setFechaCierre(null);
        }
        if (ESTADO_EN_PROGRESO.equals(destino.getNombre()) && ticket.getTecnico() == null && sesion.esStaff()) {
            ticket.setTecnico(autor);
            registrarHistorial(ticket, autor, "tecnico_asignado", null, sesion.nombre());
        }
        ticket.setEstado(destino);
        registrarHistorial(ticket, autor, "estado", origen.getNombre(), destino.getNombre());

        if (resolucion != null && ESTADO_RESUELTO.equals(destino.getNombre())) {
            Comentario comentario = new Comentario();
            comentario.setTicket(ticket);
            comentario.setAutor(autor);
            comentario.setComentario("Solución aplicada: " + resolucion);
            comentario.setInterno(false);
            comentario.setFechaComentario(LocalDateTime.now().withNano(0));
            comentarios.save(comentario);
        }
        tickets.save(ticket);
        return armarDetalle(ticket, sesion);
    }

    // ------------------------------------------------------------------
    // Gestión del equipo de soporte
    // ------------------------------------------------------------------

    @Transactional
    public TicketDetalle parchear(Integer id, PatchTicketPeticion peticion) {
        Sesion sesion = sesionServicio.actual();
        exigirStaff(sesion);
        Ticket ticket = cargarSinFiltro(id);
        Usuario autor = usuarios.getReferenceById(sesion.id());

        if (peticion.idPrioridad() != null && !peticion.idPrioridad().equals(ticket.getPrioridad().getId())) {
            Prioridad nueva = prioridades.findById(peticion.idPrioridad())
                    .orElseThrow(() -> new ReglaNegocioException("La prioridad indicada no existe"));
            String anterior = ticket.getPrioridad().getNombre();
            ticket.setPrioridad(nueva);
            // La prioridad manda sobre el SLA: la fecha límite se recalcula.
            ticket.setFechaLimite(ticket.getFechaCreacion().plusHours(nueva.getSlaHoras()));
            registrarHistorial(ticket, autor, "prioridad", anterior, nueva.getNombre());
        }
        if (peticion.idCategoria() != null && !peticion.idCategoria().equals(ticket.getCategoria().getId())) {
            Categoria nueva = categorias.findById(peticion.idCategoria())
                    .orElseThrow(() -> new ReglaNegocioException("La categoría indicada no existe"));
            String anterior = ticket.getCategoria().getNombre();
            ticket.setCategoria(nueva);
            registrarHistorial(ticket, autor, "categoria", anterior, nueva.getNombre());
        }
        tickets.save(ticket);
        return armarDetalle(ticket, sesion);
    }

    @Transactional
    public TicketDetalle asignar(Integer id, AsignacionPeticion peticion) {
        Sesion sesion = sesionServicio.actual();
        exigirStaff(sesion);
        Ticket ticket = cargarSinFiltro(id);
        Usuario autor = usuarios.getReferenceById(sesion.id());

        if (peticion.idTecnico() == null) {
            if (ticket.getTecnico() != null) {
                registrarHistorial(ticket, autor, "tecnico_asignado",
                        ticket.getTecnico().getNombreCompleto(), null);
                ticket.setTecnico(null);
            }
        } else {
            Usuario tecnico = usuarios.findById(peticion.idTecnico())
                    .orElseThrow(() -> new ReglaNegocioException("El usuario indicado no existe"));
            if (!tecnico.esStaff()) {
                throw new ReglaNegocioException("Solo se puede asignar a un técnico o administrador");
            }
            if (ticket.getTecnico() == null || !ticket.getTecnico().getId().equals(tecnico.getId())) {
                registrarHistorial(ticket, autor, "tecnico_asignado",
                        ticket.getTecnico() == null ? null : ticket.getTecnico().getNombreCompleto(),
                        tecnico.getNombreCompleto());
                ticket.setTecnico(tecnico);
            }
        }
        tickets.save(ticket);
        return armarDetalle(ticket, sesion);
    }

    // ------------------------------------------------------------------
    // Valoración (solo quien abrió el ticket, y solo cerrado)
    // ------------------------------------------------------------------

    @Transactional
    public TicketDetalle valorar(Integer id, ValoracionPeticion peticion) {
        Sesion sesion = sesionServicio.actual();
        if (sesion.esStaff()) {
            throw new SinPermisoException("La valoración la escribe quien abrió el ticket");
        }
        Ticket ticket = cargarConAcceso(id, sesion);
        if (!ESTADO_CERRADO.equals(ticket.getEstado().getNombre())) {
            throw new ReglaNegocioException("Solo se puede valorar un ticket cerrado");
        }
        if (valoraciones.existsByTicket_Id(id)) {
            throw new ReglaNegocioException("Este ticket ya tiene una valoración");
        }
        Valoracion valoracion = new Valoracion();
        valoracion.setTicket(ticket);
        valoracion.setPuntuacion(peticion.puntuacion());
        valoracion.setComentario(peticion.comentario());
        valoracion.setFechaValoracion(LocalDateTime.now().withNano(0));
        valoraciones.save(valoracion);
        return armarDetalle(ticket, sesion);
    }

    // ------------------------------------------------------------------
    // Interno
    // ------------------------------------------------------------------

    private Ticket cargarSinFiltro(Integer id) {
        return tickets.detallePorId(id)
                .orElseThrow(() -> new NoEncontradoException("Ticket no encontrado"));
    }

    /**
     * Un empleado solo puede operar sobre sus tickets. Ajeno = 404, para no
     * confirmar siquiera que existe.
     */
    private Ticket cargarConAcceso(Integer id, Sesion sesion) {
        Ticket ticket = cargarSinFiltro(id);
        if (!sesion.esStaff() && !ticket.getCreador().getId().equals(sesion.id())) {
            throw new NoEncontradoException("Ticket no encontrado");
        }
        return ticket;
    }

    private Ticket recargar(Integer id) {
        return cargarSinFiltro(id);
    }

    private TicketDetalle armarDetalle(Ticket ticket, Sesion sesion) {
        List<Comentario> listaComentarios = sesion.esStaff()
                ? comentarios.findByTicket_IdOrderByFechaComentarioAscIdAsc(ticket.getId())
                : comentarios.findByTicket_IdAndInternoFalseOrderByFechaComentarioAscIdAsc(ticket.getId());
        List<HistorialCambio> listaHistorial =
                historial.findByTicket_IdOrderByFechaCambioAscIdAsc(ticket.getId());
        Valoracion valoracion = valoraciones.findByTicket_Id(ticket.getId()).orElse(null);
        return Mapeo.detalle(ticket, listaComentarios, listaHistorial, valoracion, LocalDateTime.now());
    }

    private void registrarHistorial(Ticket ticket, Usuario autor, String campo, String anterior, String nuevo) {
        HistorialCambio cambio = new HistorialCambio();
        cambio.setTicket(ticket);
        cambio.setAutor(autor);
        cambio.setCampo(campo);
        cambio.setValorAnterior(anterior);
        cambio.setValorNuevo(nuevo);
        cambio.setFechaCambio(LocalDateTime.now().withNano(0));
        historial.save(cambio);
    }

    private void exigirStaff(Sesion sesion) {
        if (!sesion.esStaff()) {
            throw new SinPermisoException("Solo el equipo de soporte puede hacer esta operación");
        }
    }
}
