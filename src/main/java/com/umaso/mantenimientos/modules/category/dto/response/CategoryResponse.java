package com.umaso.mantenimientos.modules.category.dto.response;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String nombre,
        String descripcion
) {
}