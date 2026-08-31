package com.umaso.mantenimientos.modules.auth.dto.response;

public record AuthResponse(String accessToken, String tokenType, long expiresIn,
                           AuthenticatedUserResponse usuario) {}
