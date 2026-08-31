package com.umaso.mantenimientos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String issuer,
        String audience,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String rsaPublicKey,
        String rsaPrivateKey,
        boolean allowEphemeralDevKeys,
        List<String> corsAllowedOrigins,
        RefreshCookie refreshCookie
) {
    public record RefreshCookie(String name, boolean secure, String sameSite, String path) {}
}
