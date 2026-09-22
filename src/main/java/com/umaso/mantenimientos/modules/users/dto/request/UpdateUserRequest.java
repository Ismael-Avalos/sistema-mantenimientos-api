package com.umaso.mantenimientos.modules.users.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotNull UUID rolId,
        @NotNull Boolean activo
) {}
