package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.Prioridad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrioridadRepository extends JpaRepository<Prioridad, Integer> {

    List<Prioridad> findAllByOrderByOrdenAsc();
}
