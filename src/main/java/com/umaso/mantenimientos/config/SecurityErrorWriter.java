package com.umaso.mantenimientos.config;

import com.umaso.mantenimientos.shared.exception.ApiProblem;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {
    private final JsonMapper objectMapper;

    public void write(HttpServletRequest request, HttpServletResponse response, int status,
                      String title, String detail, String code) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), new ApiProblem(
                "https://api.mantenimientos.local/problems/" + code.toLowerCase(), title, status,
                detail, request.getRequestURI(), code, Instant.now()));
    }

    public void writeBearerFailure(HttpServletRequest request, HttpServletResponse response, Exception exception)
            throws IOException {
        StringBuilder messages = new StringBuilder();
        Throwable current = exception;
        while (current != null) {
            if (current.getMessage() != null) messages.append(' ').append(current.getMessage());
            current = current.getCause();
        }
        boolean expired = messages.toString().toLowerCase(Locale.ROOT).contains("expired");
        write(request, response, 401, "Unauthorized", "La sesión no es válida o expiró.",
                expired ? "AUTH_TOKEN_EXPIRED" : "AUTH_TOKEN_INVALID");
    }
}
