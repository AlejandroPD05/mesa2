package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.Estado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    Optional<Estado> findByNombre(String nombre);

    List<Estado> findAllByOrderByOrdenAsc();
}
