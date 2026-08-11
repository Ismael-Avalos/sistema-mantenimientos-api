package com.umaso.mantenimientos.modules.auth.dto.request;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    private UUID usuarioId; // <-- Ajustado a UUID
    private String nuevaContrasena;
}