package com.royaltown.mesa.servicio;

import com.royaltown.mesa.error.NoAutenticadoException;
import com.royaltown.mesa.error.ReglaNegocioException;
import com.royaltown.mesa.modelo.Rol;
import com.royaltown.mesa.modelo.Usuario;
import com.royaltown.mesa.repo.RolRepository;
import com.royaltown.mesa.repo.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServicio {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    public AuthServicio(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String email, String password) {
        // El mismo mensaje para usuario inexistente y contraseña mala: no se
        // confirma a nadie qué correos están dados de alta.
        Usuario usuario = usuarios.findByEmail(normalizar(email))
                .orElseThrow(() -> new NoAutenticadoException("Credenciales incorrectas"));
        if (!encoder.matches(password, usuario.getPasswordHash())) {
            throw new NoAutenticadoException("Credenciales incorrectas");
        }
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("Tu cuenta está desactivada: contacta con soporte");
        }
        return usuario;
    }

    @Transactional
    public Usuario registrar(String nombre, String email, String password) {
        String correo = normalizar(email);
        if (usuarios.existsByEmail(correo)) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo", Map.of("campo", "email"));
        }
        Rol empleado = roles.findByNombre("empleado")
                .orElseThrow(() -> new IllegalStateException("Falta el rol «empleado» en la base de datos"));
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(nombre.trim());
        usuario.setEmail(correo);
        usuario.setPasswordHash(encoder.encode(password));
        usuario.setRol(empleado);
        usuario.setActivo(true);
        usuario.setFechaAlta(LocalDateTime.now().withNano(0));
        return usuarios.save(usuario);
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
