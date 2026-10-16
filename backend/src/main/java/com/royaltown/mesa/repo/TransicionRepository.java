package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.TransicionEstado;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransicionRepository extends JpaRepository<TransicionEstado, Integer> {

    /** La matriz completa (10 filas): se carga en memoria y valida el servicio. */
    @Query("SELECT t FROM TransicionEstado t")
    @EntityGraph(attributePaths = {"origen", "destino"})
    List<TransicionEstado> todas();
}
