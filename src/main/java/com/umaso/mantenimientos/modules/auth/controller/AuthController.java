package com.umaso.mantenimientos.modules.auth.controller;

import com.umaso.mantenimientos.modules.auth.dto.response.AuthResponse;
import com.umaso.mantenimientos.modules.auth.dto.request.ChangePasswordRequest;
import com.umaso.mantenimientos.modules.auth.dto.request.LoginRequest;
import com.umaso.mantenimientos.modules.auth.service.AuthService;
import com.umaso.mantenimientos.modules.auth.service.IssuedSession;
import com.umaso.mantenimientos.modules.auth.service.RefreshCookieService;
import com.umaso.mantenimientos.config.SecurityProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshCookieService cookieService;
    private final SecurityProperties properties;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return sessionResponse(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        return sessionResponse(authService.refresh(refreshCookie(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(refreshCookie(request));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookieService.clear().toString()).build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(JwtAuthenticationToken authentication) {
        return ResponseEntity.ok(authService.me(UUID.fromString(authentication.getToken().getSubject())));
    }

    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(@Valid @RequestBody ChangePasswordRequest request,
                                                JwtAuthenticationToken authentication) {
        authService.cambiarContrasena(UUID.fromString(authentication.getToken().getSubject()), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, cookieService.clear().toString()).build();
    }

    private ResponseEntity<AuthResponse> sessionResponse(IssuedSession session) {
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                cookieService.create(session.refreshToken(), session.refreshExpiresAt()).toString())
                .body(session.response());
    }

    private String refreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> properties.refreshCookie().name().equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }
}
