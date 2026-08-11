package com.umaso.mantenimientos.modules.users.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nombre,
        String correo,
        String rol,
        Boolean activo,
        Boolean debeCambiarContrasena, // <-- Agregado para visibilidad del Frontend
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}