package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.modules.auth.dto.request.ChangePasswordRequest;
import com.umaso.mantenimientos.modules.auth.dto.request.LoginRequest;
import com.umaso.mantenimientos.modules.auth.dto.response.AuthResponse;
import com.umaso.mantenimientos.modules.auth.dto.response.AuthenticatedUserResponse;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import com.umaso.mantenimientos.config.SecurityProperties;
import com.umaso.mantenimientos.shared.exception.ApiException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final SecurityProperties properties;

    @Transactional
    public IssuedSession login(LoginRequest request) {
        String email = request.correo().trim().toLowerCase(Locale.ROOT);
        User usuario = userRepository.findByCorreoIgnoreCase(email)
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw invalidCredentials();
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_ACCOUNT_INACTIVE",
                    "La cuenta no está disponible.");
        }
        return issueSession(usuario, refreshTokenService.create(usuario));
    }

    @Transactional
    public void cambiarContrasena(UUID authenticatedUserId, ChangePasswordRequest request) {
        User usuario = userRepository.findByIdWithRole(authenticatedUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_INVALID",
                        "La sesión no es válida o expiró."));
        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasena())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_CURRENT_PASSWORD_INVALID",
                    "La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(request.nuevaContrasena(), usuario.getContrasena())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_REUSED",
                    "La nueva contraseña debe ser diferente de la actual.");
        }
        usuario.setContrasena(passwordEncoder.encode(request.nuevaContrasena()));
        usuario.setDebeCambiarContrasena(false);
        usuario.setSecurityVersion(usuario.getSecurityVersion() + 1);
        userRepository.save(usuario);
        refreshTokenService.revokeAll(usuario.getId());
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse me(UUID authenticatedUserId) {
        User user = userRepository.findByIdWithRole(authenticatedUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_INVALID",
                        "La sesión no es válida o expiró."));
        return AuthenticatedUserResponse.from(user);
    }

    @Transactional
    public IssuedSession refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID",
                    "No se proporcionó un refresh token válido.");
        }
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawToken);
        return issueSession(rotation.user(),
                new RefreshTokenService.TokenValue(rotation.raw(), null), rotation.expiresAt());
    }

    public void logout(String rawToken) { refreshTokenService.revoke(rawToken); }

    private IssuedSession issueSession(User user, RefreshTokenService.TokenValue refresh) {
        return issueSession(user, refresh, refresh.entity().getExpiresAt());
    }

    private IssuedSession issueSession(User user, RefreshTokenService.TokenValue refresh, java.time.Instant expiration) {
        String access = jwtTokenService.issue(user);
        AuthResponse response = new AuthResponse(access, "Bearer", properties.accessTokenTtl().toSeconds(),
                AuthenticatedUserResponse.from(user));
        return new IssuedSession(response, refresh.raw(), expiration);
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Credenciales incorrectas.");
    }
}
