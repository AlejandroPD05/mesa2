package com.royaltown.mesa.servicio;

import com.royaltown.mesa.dto.KbDtos.ArticuloDetalleDto;
import com.royaltown.mesa.dto.KbDtos.ArticuloPeticion;
import com.royaltown.mesa.dto.KbDtos.ArticuloResumenDto;
import com.royaltown.mesa.dto.KbDtos.FeedbackDto;
import com.royaltown.mesa.error.NoEncontradoException;
import com.royaltown.mesa.error.ReglaNegocioException;
import com.royaltown.mesa.modelo.ArticuloKb;
import com.royaltown.mesa.modelo.Categoria;
import com.royaltown.mesa.repo.ArticuloKbRepository;
import com.royaltown.mesa.repo.CategoriaRepository;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KbServicio {

    private final ArticuloKbRepository articulos;
    private final CategoriaRepository categorias;
    private final SesionServicio sesionServicio;

    public KbServicio(ArticuloKbRepository articulos, CategoriaRepository categorias,
            SesionServicio sesionServicio) {
        this.articulos = articulos;
        this.categorias = categorias;
        this.sesionServicio = sesionServicio;
    }

    @Transactional(readOnly = true)
    public List<ArticuloResumenDto> listar(String texto, Integer idCategoria, boolean incluirNoPublicados) {
        boolean staff = sesionServicio.opcional().map(Sesion::esStaff).orElse(false);
        boolean soloPublicados = !(staff && incluirNoPublicados);
        String patron = "%" + (texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT)) + "%";
        int categoriaFiltro = idCategoria == null ? -1 : idCategoria;
        return articulos.buscar(soloPublicados, categoriaFiltro, patron)
                .stream()
                .map(Mapeo::articuloResumen)
                .toList();
    }

    @Transactional
    public ArticuloDetalleDto detalle(String slug) {
        ArticuloKb articulo = articulos.findBySlug(slug)
                .orElseThrow(() -> new NoEncontradoException("Artículo no encontrado"));
        if (!articulo.isPublicado() && !esStaff()) {
            throw new NoEncontradoException("Artículo no encontrado");
        }
        articulo.setVisitas(articulo.getVisitas() + 1);
        articulos.save(articulo);
        return Mapeo.articuloDetalle(articulo);
    }

    @Transactional
    public FeedbackDto feedback(Integer id, boolean util) {
        ArticuloKb articulo = articulos.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Artículo no encontrado"));
        if (util) {
            articulo.setVotosUtil(articulo.getVotosUtil() + 1);
        } else {
            articulo.setVotosNoUtil(articulo.getVotosNoUtil() + 1);
        }
        articulos.save(articulo);
        return new FeedbackDto(articulo.getVotosUtil(), articulo.getVotosNoUtil());
    }

    @Transactional
    public ArticuloDetalleDto crear(ArticuloPeticion peticion) {
        return guardar(null, peticion);
    }

    @Transactional
    public ArticuloDetalleDto actualizar(Integer id, ArticuloPeticion peticion) {
        return guardar(id, peticion);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!articulos.existsById(id)) {
            throw new NoEncontradoException("Artículo no encontrado");
        }
        articulos.deleteById(id);
    }

    private ArticuloDetalleDto guardar(Integer id, ArticuloPeticion peticion) {
        ArticuloKb articulo = id == null
                ? new ArticuloKb()
                : articulos.findById(id).orElseThrow(() -> new NoEncontradoException("Artículo no encontrado"));
        Categoria categoria = categorias.findById(peticion.idCategoria())
                .orElseThrow(() -> new ReglaNegocioException("La categoría indicada no existe"));

        String slug = (peticion.slug() == null || peticion.slug().isBlank())
                ? slugificar(peticion.titulo())
                : peticion.slug().trim().toLowerCase(Locale.ROOT);
        if (!slug.matches("[a-z0-9-]{3,120}")) {
            throw new ReglaNegocioException("El slug solo admite minúsculas, números y guiones (3 a 120 caracteres)");
        }
        articulos.findBySlug(slug).ifPresent(otro -> {
            if (id == null || !otro.getId().equals(id)) {
                throw new ReglaNegocioException("Ya existe un artículo con ese slug", java.util.Map.of("campo", "slug"));
            }
        });

        articulo.setTitulo(peticion.titulo().trim());
        articulo.setSlug(slug);
        articulo.setResumen(peticion.resumen().trim());
        articulo.setContenido(peticion.contenido().trim());
        articulo.setCategoria(categoria);
        articulo.setPublicado(peticion.publicado());
        if (id == null) {
            articulo.setVisitas(0);
            articulo.setVotosUtil(0);
            articulo.setVotosNoUtil(0);
            articulo.setFechaCreacion(LocalDateTime.now().withNano(0));
        }
        return Mapeo.articuloDetalle(articulos.save(articulo));
    }

    private boolean esStaff() {
        return sesionServicio.opcional().map(Sesion::esStaff).orElse(false);
    }

    /** Título → slug: sin acentos, minúsculas y guiones. */
    static String slugificar(String titulo) {
        String base = Normalizer.normalize(titulo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (base.length() > 120) {
            base = base.substring(0, 120).replaceAll("-+$", "");
        }
        return base;
    }
}
