package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.dto.request.LoginRequest;
import es.ual.dra.agrotrack.dto.request.PerfilUpdateRequest;
import es.ual.dra.agrotrack.dto.request.RegisterRequest;
import es.ual.dra.agrotrack.dto.response.JwtResponse;
import es.ual.dra.agrotrack.dto.response.PerfilResponse;
import es.ual.dra.agrotrack.model.entity.AppUser;
import es.ual.dra.agrotrack.model.enums.Rol;
import es.ual.dra.agrotrack.repository.AppUserRepository;
import es.ual.dra.agrotrack.security.util.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Maneja el registro y el login.
 *
 * Registro:
 *   - Valida que el email no esté ya en uso.
 *   - Cifra la contraseña con BCrypt antes de guardarla.
 *   - Crea el usuario con rol AGRICULTOR por defecto.
 *
 * Login:
 *   - Busca por email.
 *   - Compara password en plano con el hash usando BCrypt.matches().
 *   - Genera y devuelve un JWT firmado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AppUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AppUser registrar(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
        }
        AppUser user = new AppUser();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setNombre(req.nombre());
        user.setApellidos(req.apellidos());
        user.setFoto(req.foto());
        user.setRol(Rol.AGRICULTOR);
        AppUser guardado = userRepo.save(user);
        log.info("Nuevo usuario registrado: {} (id={})", guardado.getEmail(), guardado.getId());
        return guardado;
    }

    public PerfilResponse obtenerPerfil(Long usuarioId) {
        AppUser user = userRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return PerfilResponse.from(user);
    }

    @Transactional
    public PerfilResponse editarPerfil(Long usuarioId, PerfilUpdateRequest req) {
        AppUser user = userRepo.findById(usuarioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        user.setNombre(req.nombre());
        user.setApellidos(req.apellidos());
        user.setFoto(req.foto());
        return PerfilResponse.from(userRepo.save(user));
    }

    public JwtResponse login(LoginRequest req) {
        AppUser user = userRepo.findByEmail(req.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generar(user);
        long expiresAt = System.currentTimeMillis() + jwtService.getExpiracionMs();
        return new JwtResponse(token, user.getEmail(), user.getRol(), expiresAt);
    }
}
