package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.config.SecurityProperties;
import com.umaso.mantenimientos.modules.auth.entity.RefreshToken;
import com.umaso.mantenimientos.modules.auth.repository.RefreshTokenRepository;
import com.umaso.mantenimientos.modules.roles.entity.Role;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.shared.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {
    private RefreshTokenRepository repository;
    private RefreshTokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenRepository.class);
        SecurityProperties properties = new SecurityProperties("https://issuer.test", "aud", Duration.ofMinutes(15),
                Duration.ofDays(7), "", "", true, List.of("http://localhost:5173"),
                new SecurityProperties.RefreshCookie("refresh_token", false, "Lax", "/api/auth"));
        service = new RefreshTokenService(repository, properties);
        user = User.builder().id(UUID.randomUUID()).activo(true)
                .rol(Role.builder().nombre("TECNICO").build()).build();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void persistsOnlyHashAndCreatesHighEntropyToken() {
        RefreshTokenService.TokenValue value = service.create(user);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        assertThat(value.raw()).hasSizeGreaterThanOrEqualTo(43);
        assertThat(captor.getValue().getTokenHash()).hasSize(64).doesNotContain(value.raw());
    }

    @Test
    void rotatesAndRevokesPreviousToken() {
        RefreshToken current = validToken();
        when(repository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(current));
        RefreshTokenService.Rotation rotation = service.rotate("old-token");
        assertThat(rotation.raw()).isNotEqualTo("old-token");
        assertThat(current.getRevokedAt()).isNotNull();
        assertThat(current.getReplacedBy()).isNotNull();
        assertThat(current.getReplacedBy().getFamilyId()).isEqualTo(current.getFamilyId());
    }

    @Test
    void reuseRevokesWholeFamily() {
        RefreshToken current = validToken();
        current.setRevokedAt(Instant.now().minusSeconds(1));
        current.setReplacedBy(RefreshToken.builder().build());
        when(repository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(current));
        assertThatThrownBy(() -> service.rotate("reused")).isInstanceOfSatisfying(ApiException.class,
                ex -> assertThat(ex.getCode()).isEqualTo("AUTH_REFRESH_REUSED"));
        verify(repository).revokeFamily(eq(current.getFamilyId()), any(Instant.class));
    }

    @Test
    void expiredAndRevokedTokensAreRejectedAndLogoutRevokesFamily() {
        RefreshToken expired = validToken();
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        when(repository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.rotate("expired")).isInstanceOfSatisfying(ApiException.class,
                ex -> assertThat(ex.getCode()).isEqualTo("AUTH_REFRESH_EXPIRED"));
        service.revoke("expired");
        verify(repository).revokeFamily(eq(expired.getFamilyId()), any(Instant.class));
    }

    private RefreshToken validToken() {
        return RefreshToken.builder().id(UUID.randomUUID()).usuario(user).familyId(UUID.randomUUID())
                .sessionJti(UUID.randomUUID()).createdAt(Instant.now().minusSeconds(1))
                .expiresAt(Instant.now().plusSeconds(600)).build();
    }
}
