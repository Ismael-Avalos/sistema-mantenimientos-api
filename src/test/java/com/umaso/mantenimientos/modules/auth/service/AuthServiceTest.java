package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.config.SecurityProperties;
import com.umaso.mantenimientos.modules.auth.dto.request.ChangePasswordRequest;
import com.umaso.mantenimientos.modules.auth.dto.request.LoginRequest;
import com.umaso.mantenimientos.modules.auth.entity.RefreshToken;
import com.umaso.mantenimientos.modules.roles.entity.Role;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import com.umaso.mantenimientos.shared.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.ThrowableAssert;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository users;
    private RefreshTokenService refreshTokens;
    private JwtTokenService jwtTokens;
    private PasswordEncoder encoder;
    private AuthService service;
    private User user;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        refreshTokens = mock(RefreshTokenService.class);
        jwtTokens = mock(JwtTokenService.class);
        encoder = new BCryptPasswordEncoder();
        SecurityProperties properties = new SecurityProperties("issuer", "aud", Duration.ofMinutes(15),
                Duration.ofDays(7), "", "", true, List.of("http://localhost:5173"),
                new SecurityProperties.RefreshCookie("refresh_token", false, "Lax", "/api/auth"));
        service = new AuthService(users, encoder, jwtTokens, refreshTokens, properties);
        user = User.builder().id(UUID.randomUUID()).nombre("User").correo("user@example.com")
                .contrasena(encoder.encode("Correct1!password")).activo(true).debeCambiarContrasena(false)
                .rol(Role.builder().nombre("TECNICO").build()).build();
    }

    @Test
    void validLoginNormalizesEmailAndUsesPasswordEncoder() {
        when(users.findByCorreoIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(jwtTokens.issue(user)).thenReturn("jwt");
        RefreshToken token = RefreshToken.builder().expiresAt(Instant.now().plusSeconds(60)).build();
        when(refreshTokens.create(user)).thenReturn(new RefreshTokenService.TokenValue("refresh", token));
        IssuedSession result = service.login(new LoginRequest(" USER@EXAMPLE.COM ", "Correct1!password"));
        assertThat(result.response().accessToken()).isEqualTo("jwt");
        assertThat(result.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void missingUserAndWrongPasswordHaveSameStableError() {
        when(users.findByCorreoIgnoreCase(anyString())).thenReturn(Optional.empty());
        assertCode(serviceCall("none@example.com", "bad"), "AUTH_INVALID_CREDENTIALS");
        when(users.findByCorreoIgnoreCase(anyString())).thenReturn(Optional.of(user));
        assertCode(serviceCall("user@example.com", "bad"), "AUTH_INVALID_CREDENTIALS");
    }

    @Test
    void inactiveUserIsRejected() {
        user.setActivo(false);
        when(users.findByCorreoIgnoreCase(anyString())).thenReturn(Optional.of(user));
        assertCode(serviceCall("user@example.com", "Correct1!password"), "AUTH_ACCOUNT_INACTIVE");
    }

    @Test
    void passwordChangeUsesAuthenticatedIdAndRevokesEverySession() {
        when(users.findByIdWithRole(user.getId())).thenReturn(Optional.of(user));
        service.cambiarContrasena(user.getId(),
                new ChangePasswordRequest("Correct1!password", "NewCorrect2!password"));
        assertThat(encoder.matches("NewCorrect2!password", user.getContrasena())).isTrue();
        assertThat(user.getDebeCambiarContrasena()).isFalse();
        assertThat(user.getSecurityVersion()).isEqualTo(1);
        verify(refreshTokens).revokeAll(user.getId());
    }

    private ThrowableAssert.ThrowingCallable serviceCall(String email, String password) {
        return () -> service.login(new LoginRequest(email, password));
    }

    private void assertCode(ThrowableAssert.ThrowingCallable call, String code) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ApiException.class,
                exception -> assertThat(exception.getCode()).isEqualTo(code));
    }
}
