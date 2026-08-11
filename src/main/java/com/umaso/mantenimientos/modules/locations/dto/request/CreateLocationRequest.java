package com.umaso.mantenimientos.modules.locations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLocationRequest(
        @NotBlank
        @Size(max = 100)
        String nombre,

        @Size(max = 100)
        String edificio
) {
}
