package com.umaso.mantenimientos.modules.assets.dto.response;

import com.umaso.mantenimientos.modules.assets.entity.AssetStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssetResponse(

        UUID id,

        UUID qrUuid,

        String codigoInventario,

        String nombre,

        String tipo,

        String marca,

        String modelo,

        String serialEquipo,

        AssetStatus estado,

        LocalDate fechaAdquisicion,

        LocalDateTime createdAt

) {
}