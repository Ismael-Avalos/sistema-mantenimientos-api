package com.umaso.mantenimientos.config;

import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUserSecurityFilter extends OncePerRequestFilter {
    private static final Set<String> PASSWORD_CHANGE_ALLOWED = Set.of(
            "/api/auth/me", "/api/auth/cambiar-contrasena", "/api/auth/logout", "/api/auth/refresh");
    private final UserRepository userRepository;
    private final SecurityErrorWriter errorWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return CorsUtils.isPreFlightRequest(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt) || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        User user;
        try {
            user = userRepository.findByIdWithRole(UUID.fromString(jwt.getToken().getSubject())).orElse(null);
        } catch (IllegalArgumentException ex) {
            user = null;
        }
        Long version = jwt.getToken().getClaim("ver");
        if (user == null || !Boolean.TRUE.equals(user.getActivo()) || version == null
                || version.longValue() != user.getSecurityVersion()) {
            errorWriter.write(request, response, 401, "Unauthorized",
                    "La sesión no es válida o expiró.", "AUTH_TOKEN_INVALID");
            return;
        }
        if (Boolean.TRUE.equals(user.getDebeCambiarContrasena())
                && !PASSWORD_CHANGE_ALLOWED.contains(request.getRequestURI())) {
            errorWriter.write(request, response, 403, "Forbidden",
                    "Debe cambiar la contraseña antes de continuar.", "AUTH_PASSWORD_CHANGE_REQUIRED");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
