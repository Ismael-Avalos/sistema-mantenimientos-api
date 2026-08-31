package com.umaso.mantenimientos.shared.exception;

import java.time.Instant;

public record ApiProblem(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String code,
        Instant timestamp
) {}
