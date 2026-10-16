package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.Valoracion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValoracionRepository extends JpaRepository<Valoracion, Integer> {

    Optional<Valoracion> findByTicket_Id(Integer idTicket);

    boolean existsByTicket_Id(Integer idTicket);
}
