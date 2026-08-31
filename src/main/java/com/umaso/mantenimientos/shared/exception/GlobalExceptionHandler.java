package com.umaso.mantenimientos.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiProblem> handleApi(ApiException ex, HttpServletRequest request) {
        return problem(ex.getStatus(), ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiProblem> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct().collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detail, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiProblem> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La solicitud contiene datos inválidos.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiProblem> handleDataIntegrity(DataIntegrityViolationException ex,
                                                   HttpServletRequest request) {
        DatabaseConstraint constraint = inspectConstraint(ex);

        if (constraint.isUniqueViolation()) {
            log.warn("Conflicto por restricción única [{}] en {} {}",
                    constraint.name(), request.getMethod(), request.getRequestURI());
            return problem(HttpStatus.CONFLICT, "Recurso Duplicado", "DUPLICATE_ENTRY",
                    duplicateDetail(constraint.name()), request);
        }

        log.warn("Violación de integridad de datos (SQLState={}, restricción={}) en {} {}",
                constraint.sqlState(), constraint.name(), request.getMethod(), request.getRequestURI());
        return problem(HttpStatus.BAD_REQUEST, "Conflicto de Integridad de Datos", "DATA_INTEGRITY_ERROR",
                integrityDetail(constraint.sqlState()), request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiProblem> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiProblem> handleConflict(IllegalStateException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiProblem> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiProblem> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error inesperado.", request);
    }

    private ResponseEntity<ApiProblem> problem(HttpStatus status, String code, String detail,
                                                HttpServletRequest request) {
        return problem(status, status.getReasonPhrase(), code, detail, request);
    }

    private ResponseEntity<ApiProblem> problem(HttpStatus status, String title, String code, String detail,
                                                HttpServletRequest request) {
        ApiProblem body = new ApiProblem("https://api.mantenimientos.local/problems/" + code.toLowerCase(),
                title, status.value(), detail, request.getRequestURI(), code, Instant.now());
        return ResponseEntity.status(status).header("Content-Type", "application/problem+json").body(body);
    }

    private DatabaseConstraint inspectConstraint(Throwable exception) {
        String sqlState = null;
        String constraintName = null;
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Throwable cause = exception; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException && sqlState == null) {
                sqlState = sqlException.getSQLState();
            }
            if (cause instanceof org.hibernate.exception.ConstraintViolationException hibernateException
                    && constraintName == null) {
                constraintName = hibernateException.getConstraintName();
            }
        }
        return new DatabaseConstraint(sqlState, constraintName);
    }

    private String duplicateDetail(String constraintName) {
        if (constraintName == null) {
            return "Ya existe un registro con uno de los valores únicos proporcionados.";
        }

        String normalized = constraintName.toLowerCase();
        if (normalized.contains("serial_equipo")) {
            return "El número de serie ingresado ya se encuentra registrado en el sistema.";
        }
        if (normalized.contains("codigo_inventario")) {
            return "El código de inventario ingresado ya se encuentra registrado en el sistema.";
        }
        if (normalized.contains("qr_uuid")) {
            return "El código QR ingresado ya se encuentra registrado en el sistema.";
        }
        if (normalized.contains("numero_reporte")) {
            return "El número de reporte ya se encuentra registrado en el sistema.";
        }
        if (normalized.contains("correo")) {
            return "El correo electrónico ingresado ya se encuentra registrado en el sistema.";
        }
        if (normalized.contains("uq_ubicaciones_nombre_edificio")) {
            return "Ya existe una ubicación con ese nombre en el edificio indicado.";
        }
        if (normalized.contains("categorias_nombre")) {
            return "Ya existe una categoría con el nombre ingresado.";
        }
        if (normalized.contains("nombre")) {
            return "El nombre ingresado ya se encuentra registrado en el sistema.";
        }
        return "Ya existe un registro con uno de los valores únicos proporcionados.";
    }

    private String integrityDetail(String sqlState) {
        if ("23502".equals(sqlState)) {
            return "Falta un dato obligatorio para completar la operación.";
        }
        if ("23503".equals(sqlState)) {
            return "La operación hace referencia a un recurso inexistente o que todavía está en uso.";
        }
        if ("23514".equals(sqlState)) {
            return "Uno de los valores proporcionados no cumple las reglas de integridad requeridas.";
        }
        return "Los datos proporcionados no cumplen las restricciones de integridad requeridas.";
    }

    private record DatabaseConstraint(String sqlState, String name) {
        boolean isUniqueViolation() {
            return "23505".equals(sqlState);
        }
    }
}
