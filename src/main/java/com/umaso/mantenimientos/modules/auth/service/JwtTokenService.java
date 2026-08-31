package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.config.SecurityProperties;
import com.umaso.mantenimientos.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtEncoder jwtEncoder;
    private final SecurityProperties properties;

    public String issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(properties.issuer())
                .audience(List.of(properties.audience())).subject(user.getId().toString())
                .issuedAt(now).expiresAt(now.plus(properties.accessTokenTtl()))
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of(normalizeRole(user.getRol().getNombre())))
                .claim("pwd_change_required", user.getDebeCambiarContrasena())
                .claim("ver", user.getSecurityVersion()).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String normalizeRole(String role) {
        String normalized = Normalizer.normalize(role.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
        String canonical = switch (normalized) {
            case "ADMIN", "ADMINISTRADOR" -> "ADMIN";
            case "TECNICO" -> "TECNICO";
            default -> normalized;
        };
        return "ROLE_" + canonical;
    }
}
