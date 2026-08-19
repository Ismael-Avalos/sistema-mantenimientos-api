package com.umaso.mantenimientos.modules.maintenances.dto.response;

import com.umaso.mantenimientos.modules.maintenances.entity.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id,
        Long numeroReporte,
        MaintenanceType tipo,
        LocalDateTime fecha,
        LocalDateTime fechaEntrega,

        String sede,
        String unidad,
        String solicitanteNombre,
        String solicitanteCorreo,
        String solicitanteTelefono,

        String descripcionFalla,
        String actividadesRealizadas,
        String observacionesTecnicas,
        String recomendaciones,

        BigDecimal costo,

        UUID responsableId,
        String responsableNombre
) {}