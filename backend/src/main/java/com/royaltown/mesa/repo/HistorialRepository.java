package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.HistorialCambio;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialRepository extends JpaRepository<HistorialCambio, Integer> {

    @EntityGraph(attributePaths = "autor")
    List<HistorialCambio> findByTicket_IdOrderByFechaCambioAscIdAsc(Integer idTicket);
}
