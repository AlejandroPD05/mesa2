package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmail(String email);

    /** Para el /me: carga el rol en la misma consulta (open-in-view está apagado). */
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> detallePorId(@Param("id") Integer id);

    /** Técnicos y administradores: quienes pueden recibir una asignación. */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol r "
            + "WHERE r.nombre IN ('tecnico', 'administrador') "
            + "ORDER BY u.nombreCompleto")
    List<Usuario> equipoDeSoporte();

    boolean existsByEmail(String email);
}
