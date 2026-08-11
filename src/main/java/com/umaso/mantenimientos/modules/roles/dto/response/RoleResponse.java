package com.umaso.mantenimientos.modules.roles.dto.response;

import java.util.UUID;

public record RoleResponse(
        UUID id,
        String nombre,
        String descripcion
) {}