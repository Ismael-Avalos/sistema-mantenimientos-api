package com.umaso.mantenimientos.modules.users.service;

import com.umaso.mantenimientos.modules.roles.entity.Role;
import com.umaso.mantenimientos.modules.roles.repository.RoleRepository;
import com.umaso.mantenimientos.modules.users.dto.request.CreateUserRequest;
import com.umaso.mantenimientos.modules.users.dto.request.UpdateUserRequest;
import com.umaso.mantenimientos.modules.users.dto.request.ResetPasswordRequest;
import com.umaso.mantenimientos.modules.auth.service.RefreshTokenService;
import com.umaso.mantenimientos.modules.maintenances.repository.MaintenanceRepository;
import com.umaso.mantenimientos.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import com.umaso.mantenimientos.modules.users.dto.response.UserResponse;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final MaintenanceRepository maintenanceRepository;

    @Transactional
    public UserResponse create(CreateUserRequest request) {

        String email = request.correo().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByCorreoIgnoreCase(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "USER_EMAIL_EXISTS", "Ya existe un usuario con ese correo.");
        }

        Role role = roleRepository.findById(request.rolId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado."));

        User user = User.builder()
                .nombre(request.nombre().trim())
                .correo(email)
                .contrasena(passwordEncoder.encode(request.contrasena()))
                .rol(role)
                .activo(true)
                .debeCambiarContrasena(true)
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return mapToResponse(requireUser(id));
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request, UUID actorId) {
        User user = requireUser(id);
        Role role = roleRepository.findById(request.rolId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado."));
        boolean roleChanged = !user.getRol().getId().equals(role.getId());
        if (id.equals(actorId) && (roleChanged || !request.activo())) {
            throw selfModification();
        }
        String email = request.correo().trim().toLowerCase(Locale.ROOT);
        userRepository.findByCorreoIgnoreCase(email).filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "USER_EMAIL_EXISTS",
                            "Ya existe un usuario con ese correo.");
                });
        boolean sessionChanged = roleChanged || !user.getActivo().equals(request.activo())
                || !user.getCorreo().equals(email);
        user.setNombre(request.nombre().trim());
        user.setCorreo(email);
        user.setRol(role);
        user.setActivo(request.activo());
        if (sessionChanged) invalidateSessions(user);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(UUID id, boolean active, UUID actorId) {
        User user = requireUser(id);
        if (id.equals(actorId) && !active) throw selfModification();
        if (user.getActivo() != active) {
            user.setActivo(active);
            invalidateSessions(user);
        }
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void resetPassword(UUID id, ResetPasswordRequest request) {
        User user = requireUser(id);
        if (passwordEncoder.matches(request.contrasenaTemporal(), user.getContrasena())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_REUSED",
                    "La contraseña temporal debe ser diferente de la actual.");
        }
        user.setContrasena(passwordEncoder.encode(request.contrasenaTemporal()));
        user.setDebeCambiarContrasena(true);
        invalidateSessions(user);
        userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id, UUID actorId) {
        User user = requireUser(id);
        if (id.equals(actorId)) throw selfModification();
        if (maintenanceRepository.existsByResponsableId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "USER_HAS_MAINTENANCES",
                    "El usuario tiene mantenimientos asociados. Desactive su cuenta para conservar el historial.");
        }
        refreshTokenService.revokeAll(id);
        userRepository.delete(user);
        userRepository.flush();
    }

    private User requireUser(UUID id) {
        return userRepository.findByIdWithRole(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado."));
    }

    private void invalidateSessions(User user) {
        user.setSecurityVersion(user.getSecurityVersion() + 1);
        refreshTokenService.revokeAll(user.getId());
    }

    private ApiException selfModification() {
        return new ApiException(HttpStatus.CONFLICT, "USER_SELF_MODIFICATION",
                "No puede eliminar, desactivar ni cambiar el rol de su propia cuenta.");
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNombre(),
                user.getCorreo(),
                user.getRol().getNombre(),
                user.getActivo(),
                user.getDebeCambiarContrasena(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}
