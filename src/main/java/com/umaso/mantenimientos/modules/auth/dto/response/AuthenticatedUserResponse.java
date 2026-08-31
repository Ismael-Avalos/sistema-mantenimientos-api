package com.umaso.mantenimientos.modules.auth.dto.response;

import com.umaso.mantenimientos.modules.users.entity.User;
import java.util.UUID;

public record AuthenticatedUserResponse(UUID id, String nombre, String correo, String rol,
                                        boolean activo, boolean debeCambiarContrasena) {
    public static AuthenticatedUserResponse from(User user) {
        return new AuthenticatedUserResponse(user.getId(), user.getNombre(), user.getCorreo(),
                user.getRol().getNombre(), user.getActivo(), user.getDebeCambiarContrasena());
    }
}
