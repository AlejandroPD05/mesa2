package com.royaltown.mesa.web;

import com.royaltown.mesa.config.AjustesMesa;
import com.royaltown.mesa.dto.AuthDtos.LoginPeticion;
import com.royaltown.mesa.dto.AuthDtos.RegistroPeticion;
import com.royaltown.mesa.dto.AuthDtos.UsuarioDto;
import com.royaltown.mesa.error.NoEncontradoException;
import com.royaltown.mesa.modelo.Usuario;
import com.royaltown.mesa.repo.UsuarioRepository;
import com.royaltown.mesa.servicio.AuthServicio;
import com.royaltown.mesa.servicio.JwtServicio;
import com.royaltown.mesa.servicio.Mapeo;
import com.royaltown.mesa.servicio.SesionServicio;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServicio auth;
    private final JwtServicio jwt;
    private final AjustesMesa ajustes;
    private final UsuarioRepository usuarios;
    private final SesionServicio sesionServicio;

    public AuthController(AuthServicio auth, JwtServicio jwt, AjustesMesa ajustes,
            UsuarioRepository usuarios, SesionServicio sesionServicio) {
        this.auth = auth;
        this.jwt = jwt;
        this.ajustes = ajustes;
        this.usuarios = usuarios;
        this.sesionServicio = sesionServicio;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioDto> login(@Valid @RequestBody LoginPeticion peticion,
            HttpServletResponse respuesta) {
        Usuario usuario = auth.autenticar(peticion.email(), peticion.password());
        ponerCookieSesion(respuesta, usuario);
        return ResponseEntity.ok(Mapeo.usuario(usuario));
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDto> registro(@Valid @RequestBody RegistroPeticion peticion,
            HttpServletResponse respuesta) {
        Usuario usuario = auth.registrar(peticion.nombre(), peticion.email(), peticion.password());
        ponerCookieSesion(respuesta, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(Mapeo.usuario(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse respuesta) {
        ResponseCookie cookie = ResponseCookie.from(ajustes.cookie().nombre(), "")
                .httpOnly(true)
                .secure(ajustes.cookie().secure())
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        respuesta.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UsuarioDto me() {
        Integer id = sesionServicio.actual().id();
        Usuario usuario = usuarios.detallePorId(id)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
        return Mapeo.usuario(usuario);
    }

    private void ponerCookieSesion(HttpServletResponse respuesta, Usuario usuario) {
        ResponseCookie cookie = ResponseCookie.from(ajustes.cookie().nombre(), jwt.crear(usuario))
                .httpOnly(true)
                .secure(ajustes.cookie().secure())
                .path("/")
                // Lax: la cookie no viaja en peticiones mutantes cross-site.
                .sameSite("Lax")
                .maxAge(Duration.ofHours(ajustes.jwt().horasExpiracion()))
                .build();
        respuesta.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
