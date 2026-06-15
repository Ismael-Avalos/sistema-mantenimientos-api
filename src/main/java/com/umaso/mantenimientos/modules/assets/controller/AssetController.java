package com.umaso.mantenimientos.modules.assets.controller;

import com.umaso.mantenimientos.modules.assets.dto.request.CreateAssetRequest;
import com.umaso.mantenimientos.modules.assets.dto.response.AssetResponse;
import com.umaso.mantenimientos.modules.assets.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/maintenances/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public AssetResponse create(
            @Valid @RequestBody CreateAssetRequest request
    ) {
        return assetService.create(request);
    }

    @GetMapping
    public List<AssetResponse> findAll() {
        return assetService.findAll();
    }

    @GetMapping("/{id}")
    public AssetResponse findById(
            @PathVariable UUID id
    ) {
        return assetService.findById(id);
    }
}