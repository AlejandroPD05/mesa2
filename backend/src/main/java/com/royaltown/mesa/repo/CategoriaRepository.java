package com.royaltown.mesa.repo;

import com.royaltown.mesa.modelo.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findAllByOrderByNombreAsc();
}
