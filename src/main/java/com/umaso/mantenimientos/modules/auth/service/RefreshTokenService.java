package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.config.SecurityProperties;
import com.umaso.mantenimientos.modules.auth.entity.RefreshToken;
import com.umaso.mantenimientos.modules.auth.repository.RefreshTokenRepository;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final SecurityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public TokenValue create(User user) { return create(user, UUID.randomUUID()); }

    @Transactional(noRollbackFor = ApiException.class)
    public Rotation rotate(String rawToken) {
        RefreshToken current = repository.findByTokenHashWithUser(hash(rawToken))
                .orElseThrow(() -> invalid("AUTH_REFRESH_INVALID", "El refresh token no es válido."));
        Instant now = Instant.now();
        if (current.getRevokedAt() != null) {
            if (current.getReplacedBy() != null) {
                repository.revokeFamily(current.getFamilyId(), now);
                throw invalid("AUTH_REFRESH_REUSED", "Se detectó reutilización de una sesión.");
            }
            throw invalid("AUTH_REFRESH_INVALID", "El refresh token fue revocado.");
        }
        if (!current.getExpiresAt().isAfter(now) || !Boolean.TRUE.equals(current.getUsuario().getActivo())) {
            current.setRevokedAt(now);
            throw invalid("AUTH_REFRESH_EXPIRED", "El refresh token venció o ya no es válido.");
        }
        TokenValue successor = create(current.getUsuario(), current.getFamilyId());
        current.setRevokedAt(now);
        current.setReplacedBy(successor.entity());
        repository.save(current);
        return new Rotation(current.getUsuario(), successor.raw(), successor.entity().getExpiresAt());
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        repository.findByTokenHashWithUser(hash(rawToken)).ifPresent(token ->
                repository.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    @Transactional
    public void revokeAll(UUID userId) { repository.revokeAllForUser(userId, Instant.now()); }

    @Transactional
    public int purgeExpired() {
        return repository.deleteExpiredBefore(Instant.now().minus(properties.refreshTokenTtl()));
    }

    private TokenValue create(User user, UUID familyId) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = Instant.now();
        RefreshToken entity = repository.save(RefreshToken.builder().usuario(user).tokenHash(hash(raw))
                .familyId(familyId).sessionJti(UUID.randomUUID()).createdAt(now)
                .expiresAt(now.plus(properties.refreshTokenTtl())).build());
        return new TokenValue(raw, entity);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    private ApiException invalid(String code, String detail) {
        return new ApiException(HttpStatus.UNAUTHORIZED, code, detail);
    }

    public record TokenValue(String raw, RefreshToken entity) {}
    public record Rotation(User user, String raw, Instant expiresAt) {}
}
