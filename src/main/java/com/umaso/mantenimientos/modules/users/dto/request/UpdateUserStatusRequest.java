package com.umaso.mantenimientos.modules.users.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull Boolean activo) {}
