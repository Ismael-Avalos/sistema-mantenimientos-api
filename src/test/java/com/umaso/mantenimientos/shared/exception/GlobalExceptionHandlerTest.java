package com.umaso.mantenimientos.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = request();

    @Test
    void returnsConflictAndFriendlyMessageForDuplicatedAssetSerial() {
        SQLException sqlException = new SQLException("duplicate key", "23505");
        ConstraintViolationException hibernateException = mock(ConstraintViolationException.class);
        when(hibernateException.getConstraintName()).thenReturn("equipos_serial_equipo_key");
        when(hibernateException.getCause()).thenReturn(sqlException);

        ResponseEntity<ApiProblem> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("could not execute statement", hibernateException), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Recurso Duplicado");
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_ENTRY");
        assertThat(response.getBody().detail())
                .isEqualTo("El número de serie ingresado ya se encuentra registrado en el sistema.");
    }

    @Test
    void returnsBadRequestForForeignKeyViolation() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "could not execute statement", new SQLException("foreign key violation", "23503"));

        ResponseEntity<ApiProblem> response = handler.handleDataIntegrity(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Conflicto de Integridad de Datos");
        assertThat(response.getBody().code()).isEqualTo("DATA_INTEGRITY_ERROR");
        assertThat(response.getBody().detail()).contains("recurso inexistente");
    }

    @Test
    void returnsContextualMessagesForCategoryUserAndLocationDuplicates() {
        assertDuplicateDetail("categorias_nombre_key", "Ya existe una categoría con el nombre ingresado.");
        assertDuplicateDetail("usuarios_correo_key",
                "El correo electrónico ingresado ya se encuentra registrado en el sistema.");
        assertDuplicateDetail("uq_ubicaciones_nombre_edificio",
                "Ya existe una ubicación con ese nombre en el edificio indicado.");
    }

    @Test
    void returnsGenericBadRequestWithoutLeakingDatabaseDetails() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("sensitive database message");

        ResponseEntity<ApiProblem> response = handler.handleDataIntegrity(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().detail()).doesNotContain("sensitive database message");
    }

    private HttpServletRequest request() {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn("POST");
        when(servletRequest.getRequestURI()).thenReturn("/maintenances/assets");
        return servletRequest;
    }

    private void assertDuplicateDetail(String constraintName, String expectedDetail) {
        SQLException sqlException = new SQLException("duplicate key", "23505");
        ConstraintViolationException hibernateException = mock(ConstraintViolationException.class);
        when(hibernateException.getConstraintName()).thenReturn(constraintName);
        when(hibernateException.getCause()).thenReturn(sqlException);

        ResponseEntity<ApiProblem> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("could not execute statement", hibernateException), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_ENTRY");
        assertThat(response.getBody().detail()).isEqualTo(expectedDetail);
    }
}
