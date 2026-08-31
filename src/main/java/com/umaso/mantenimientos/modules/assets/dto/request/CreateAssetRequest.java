package com.umaso.mantenimientos.modules.assets.dto.request;

import com.umaso.mantenimientos.modules.assets.entity.AssetStatus;
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

        @Size(max = 50)
        String marca,

        @Size(max = 50)
        String modelo,

        @Size(max = 100)
        String serialEquipo,

        AssetStatus estado,

        UUID ubicacionId,

        UUID categoriaId,

        LocalDate fechaAdquisicion

) {
}
