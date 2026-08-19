package com.umaso.mantenimientos.modules.assets.dto.request;

import com.umaso.mantenimientos.modules.assets.entity.AssetStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAssetStatusRequest(
        @NotNull(message = "El estado no puede ser nulo")
        AssetStatus estado
) {}