package com.umaso.mantenimientos.modules.locations.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LocationResponse (
        UUID id,
        String nombre,
        String edificio,
        LocalDateTime createdAt
) {
}
