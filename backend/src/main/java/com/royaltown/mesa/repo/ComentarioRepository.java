package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.Comentario;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    @EntityGraph(attributePaths = "autor")
    List<Comentario> findByTicket_IdOrderByFechaComentarioAscIdAsc(Integer idTicket);

    /** Versión para empleados: las notas internas no salen del equipo de soporte. */
    @EntityGraph(attributePaths = "autor")
    List<Comentario> findByTicket_IdAndInternoFalseOrderByFechaComentarioAscIdAsc(Integer idTicket);
}
