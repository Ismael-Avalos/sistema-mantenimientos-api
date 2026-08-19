package com.umaso.mantenimientos.modules.maintenances.dto.request;

import com.umaso.mantenimientos.modules.maintenances.entity.MaintenanceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateMaintenanceRequest(

        @NotNull(message = "El ID del equipo es obligatorio")
        UUID equipoId,

        UUID responsableId,

        @NotNull(message = "El tipo de mantenimiento es obligatorio")
        MaintenanceType tipo,

        @NotBlank(message = "La sede es obligatoria")
        @Size(max = 100, message = "La sede no debe superar los 100 caracteres")
        String sede,

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

        LocalDateTime fecha,

        LocalDateTime fechaEntrega
) {}