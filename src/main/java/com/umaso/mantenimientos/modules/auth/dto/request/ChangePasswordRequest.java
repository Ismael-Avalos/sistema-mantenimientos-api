package com.umaso.mantenimientos.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String contrasenaActual,
        @NotBlank
        @Size(min = 8, max = 128, message = "La nueva contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(regexp = "(?s).*[0-9].*", message = "La nueva contraseña debe incluir al menos un número")
        String nuevaContrasena) {}
