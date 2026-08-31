package com.umaso.mantenimientos.config;

import com.umaso.mantenimientos.modules.auth.service.JwtTokenService;
import com.umaso.mantenimientos.modules.roles.entity.Role;
import com.umaso.mantenimientos.modules.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {
    private SecurityConfig config;
    private SecurityProperties properties;
    private KeyPair keyPair;
    private JwtEncoder encoder;
    private JwtDecoder decoder;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        config = new SecurityConfig();
        properties = properties("https://issuer.test", "audience");
        keyPair = config.jwtKeyPair(properties);
        encoder = config.jwtEncoder(keyPair);
        decoder = config.jwtDecoder(keyPair, properties);
        user = User.builder().id(UUID.randomUUID()).nombre("Admin").correo("admin@example.com")
                .contrasena("hash").activo(true).debeCambiarContrasena(false).securityVersion(3)
                .rol(Role.builder().nombre("ADMIN").build()).build();
    }

    @Test
    void signsAndContainsRequiredClaims() {
        String token = new JwtTokenService(encoder, properties).issue(user);
        Jwt jwt = decoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
        assertThat(jwt.getId()).isNotBlank();
        assertThat(jwt.getIssuer().toString()).isEqualTo("https://issuer.test");
        assertThat(jwt.getAudience()).containsExactly("audience");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_ADMIN");
        assertThat(((Number) jwt.getClaim("ver")).longValue()).isEqualTo(3);
    }

    @Test
    void mapsDatabaseRoleNamesToCanonicalAuthorities() {
        JwtTokenService tokens = new JwtTokenService(encoder, properties);
        user.getRol().setNombre("ADMINISTRADOR");
        assertThat(decoder.decode(tokens.issue(user)).getClaimAsStringList("roles"))
                .containsExactly("ROLE_ADMIN");

        user.getRol().setNombre("TÉCNICO");
        assertThat(decoder.decode(tokens.issue(user)).getClaimAsStringList("roles"))
                .containsExactly("ROLE_TECNICO");

        user.getRol().setNombre("PRUEBA");
        assertThat(decoder.decode(tokens.issue(user)).getClaimAsStringList("roles"))
                .containsExactly("ROLE_PRUEBA");
    }

    @Test
    void rejectsExpiredWrongIssuerAndWrongAudience() {
        assertRejected(claims("https://issuer.test", "audience", Instant.now().minusSeconds(180), Instant.now().minusSeconds(120)));
        assertRejected(claims("https://other.test", "audience", Instant.now(), Instant.now().plusSeconds(60)));
        assertRejected(claims("https://issuer.test", "other", Instant.now(), Instant.now().plusSeconds(60)));
    }

    @Test
    void rejectsInvalidSignature() throws Exception {
        KeyPair otherPair = config.jwtKeyPair(properties);
        String token = config.jwtEncoder(otherPair).encode(JwtEncoderParameters.from(
                claims("https://issuer.test", "audience", Instant.now(), Instant.now().plusSeconds(60)))).getTokenValue();
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    private void assertRejected(JwtClaimsSet claims) {
        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    private JwtClaimsSet claims(String issuer, String audience, Instant issuedAt, Instant expiresAt) {
        return JwtClaimsSet.builder().issuer(issuer).audience(List.of(audience)).subject(UUID.randomUUID().toString())
                .issuedAt(issuedAt).expiresAt(expiresAt).id(UUID.randomUUID().toString()).build();
    }

    static SecurityProperties properties(String issuer, String audience) {
        return new SecurityProperties(issuer, audience, Duration.ofMinutes(15), Duration.ofDays(7), "", "", true,
                List.of("http://localhost:5173"),
                new SecurityProperties.RefreshCookie("refresh_token", false, "Lax", "/api/auth"));
    }
}
