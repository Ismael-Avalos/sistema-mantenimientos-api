package com.umaso.mantenimientos.modules.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateUserRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        String correo,

        @NotBlank(message = "La contraseña temporal es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña temporal debe tener entre 8 y 128 caracteres")
        String contrasena,

        @NotNull(message = "El rol es obligatorio")
        UUID rolId
) {}
