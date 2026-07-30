package com.umaso.mantenimientos.modules.users.dto.request;

import java.util.UUID;

public record CreateUserRequest(
        String nombre,
        String correo,
        String contrasena,
        UUID rolId
) {}