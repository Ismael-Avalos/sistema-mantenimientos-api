package com.umaso.mantenimientos.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String contrasenaActual,
        @NotBlank @Size(min = 12, max = 128)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "debe incluir mayúscula, minúscula, número y símbolo")
        String nuevaContrasena) {}
