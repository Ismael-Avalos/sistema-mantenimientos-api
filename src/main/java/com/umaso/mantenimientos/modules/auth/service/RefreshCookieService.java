package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshCookieService {
    private final SecurityProperties properties;

    public ResponseCookie create(String token, Instant expiresAt) {
        Duration maxAge = Duration.between(Instant.now(), expiresAt);
        return base(token).maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge).build();
    }

    public ResponseCookie clear() { return base("").maxAge(Duration.ZERO).build(); }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        var cookie = properties.refreshCookie();
        return ResponseCookie.from(cookie.name(), value).httpOnly(true).secure(cookie.secure())
                .sameSite(cookie.sameSite()).path(cookie.path());
    }
}
