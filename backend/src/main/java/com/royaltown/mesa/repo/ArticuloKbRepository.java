package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.ArticuloKb;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticuloKbRepository extends JpaRepository<ArticuloKb, Integer> {

    @EntityGraph(attributePaths = "categoria")
    Optional<ArticuloKb> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /**
     * Buscador del centro de ayuda. Sin parámetros nulos (PostgreSQL no puede
     * tipar un {@code ? IS NULL} en todas las posiciones): el texto vacío llega
     * como patrón {@code %} y la categoría ausente como {@code -1}.
     */
    @Query("""
            SELECT a FROM ArticuloKb a
            WHERE (:soloPublicados = FALSE OR a.publicado = TRUE)
              AND (:categoriaFiltro < 0 OR a.categoria.id = :categoriaFiltro)
              AND (LOWER(a.titulo) LIKE :patron
                   OR LOWER(a.resumen) LIKE :patron
                   OR LOWER(a.contenido) LIKE :patron)
            ORDER BY a.visitas DESC, a.titulo ASC
            """)
    @EntityGraph(attributePaths = "categoria")
    List<ArticuloKb> buscar(@Param("soloPublicados") boolean soloPublicados,
                            @Param("categoriaFiltro") int categoriaFiltro,
                            @Param("patron") String patron);
}
