package com.umaso.mantenimientos.modules.assets.controller;

import com.umaso.mantenimientos.modules.assets.dto.request.CreateAssetRequest;
import com.umaso.mantenimientos.modules.assets.dto.response.AssetResponse;
import com.umaso.mantenimientos.modules.assets.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/maintenances/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<AssetResponse> create(
            @Valid @RequestBody CreateAssetRequest request
    ) {
        AssetResponse response = assetService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AssetResponse>> findAll() {
        return ResponseEntity.ok(assetService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(assetService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAssetRequest request
    ) {
        AssetResponse response = assetService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // BUSCAR POR CÓDIGO QR
    // ==========================================
    @GetMapping("/qr/{qrUuid}")
    public ResponseEntity<AssetResponse> findByQrUuid(
            @PathVariable UUID qrUuid
    ) {
        return ResponseEntity.ok(assetService.findByQrUuid(qrUuid));
    }
}