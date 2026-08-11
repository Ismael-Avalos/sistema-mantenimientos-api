package com.umaso.mantenimientos.modules.auth.dto.response;

import com.umaso.mantenimientos.modules.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private User usuario;
}