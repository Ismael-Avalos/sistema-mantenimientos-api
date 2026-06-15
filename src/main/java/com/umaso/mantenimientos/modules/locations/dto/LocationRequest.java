package com.umaso.mantenimientos.modules.locations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @NotBlank
        @Size(max = 100)
        String nombre,

        @Size(max = 100)
        String edificio
) {
}
