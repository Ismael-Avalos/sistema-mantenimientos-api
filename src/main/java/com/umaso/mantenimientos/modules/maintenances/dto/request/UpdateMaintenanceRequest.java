package com.umaso.mantenimientos.modules.maintenances.dto.request;

import com.umaso.mantenimientos.modules.maintenances.entity.MaintenanceType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Campos editables de un mantenimiento. El equipo y la sede son inmutables. */
public record UpdateMaintenanceRequest(
        UUID responsableId,

        @NotNull(message = "El tipo de mantenimiento es obligatorio")
        MaintenanceType tipo,

        @NotBlank(message = "El nombre del solicitante es obligatorio")
        @Size(max = 150)
        String solicitanteNombre,

        @NotBlank(message = "El correo del solicitante es obligatorio")
        @Email(message = "Debe ingresar un correo electrónico válido")
        @Size(max = 150)
        String solicitanteCorreo,

        @Size(max = 30)
        String solicitanteTelefono,

        @NotBlank(message = "La unidad o departamento es obligatorio")
        @Size(max = 150)
        String unidad,

        @NotBlank(message = "La descripción de la falla es obligatoria")
        String descripcionFalla,

        @NotBlank(message = "Las actividades realizadas son obligatorias")
        String actividadesRealizadas,

        String observacionesTecnicas,
        String recomendaciones,

        @NotNull(message = "El costo es obligatorio")
        @PositiveOrZero(message = "El costo no puede ser negativo")
        BigDecimal costo,

        @NotNull(message = "La fecha de solicitud es obligatoria")
        LocalDateTime fecha,

        LocalDateTime fechaEntrega
) {}
