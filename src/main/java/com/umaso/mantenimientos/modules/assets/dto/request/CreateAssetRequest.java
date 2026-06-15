package com.umaso.mantenimientos.modules.assets.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAssetRequest(

        @NotBlank
        @Size(max = 50)
        String codigoInventario,

        @NotBlank
        @Size(max = 100)
        String nombre,

        @NotBlank
        @Size(max = 50)
        String tipo,

        @Size(max = 50)
        String marca,

        @Size(max = 50)
        String modelo,

        @Size(max = 100)
        String serialEquipo,

        UUID ubicacionId,

        LocalDate fechaAdquisicion

) {
}