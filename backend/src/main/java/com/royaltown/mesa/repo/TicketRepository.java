package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.Ticket;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Integer>, JpaSpecificationExecutor<Ticket> {

    /** Carga el ticket con todo lo que necesita un DTO en una sola consulta. */
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    @EntityGraph(attributePaths = {"estado", "prioridad", "categoria", "creador", "tecnico"})
    Optional<Ticket> detallePorId(@Param("id") Integer id);
}
