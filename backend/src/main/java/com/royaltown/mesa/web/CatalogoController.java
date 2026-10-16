package com.royaltown.mesa.web;

import com.royaltown.mesa.dto.CatalogoDtos.CatalogoDto;
import com.royaltown.mesa.servicio.CatalogoServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogoServicio catalogo;

    public CatalogoController(CatalogoServicio catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    public CatalogoDto catalogo() {
        return catalogo.catalogo();
    }
}
