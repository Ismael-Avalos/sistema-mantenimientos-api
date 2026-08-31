package com.umaso.mantenimientos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthOriginFilter extends OncePerRequestFilter {
    private static final Set<String> PROTECTED_PATHS = Set.of("/api/auth/refresh", "/api/auth/logout");
    private final SecurityProperties properties;
    private final SecurityErrorWriter errorWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return CorsUtils.isPreFlightRequest(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (PROTECTED_PATHS.contains(request.getRequestURI())) {
            String origin = request.getHeader("Origin");
            if (origin != null && !properties.corsAllowedOrigins().contains(origin)) {
                errorWriter.write(request, response, 403, "Forbidden",
                        "El origen de la solicitud no está permitido.", "AUTH_ORIGIN_DENIED");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
