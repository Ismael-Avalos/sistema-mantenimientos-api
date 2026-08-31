package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.modules.auth.dto.response.AuthResponse;
import java.time.Instant;

public record IssuedSession(AuthResponse response, String refreshToken, Instant refreshExpiresAt) {}
