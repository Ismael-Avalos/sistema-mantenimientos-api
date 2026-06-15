package com.umaso.mantenimientos.modules.assets.service;

import com.umaso.mantenimientos.modules.assets.dto.request.CreateAssetRequest;
import com.umaso.mantenimientos.modules.assets.dto.response.AssetResponse;
import com.umaso.mantenimientos.modules.assets.entity.Asset;
import com.umaso.mantenimientos.modules.assets.entity.AssetStatus;
import com.umaso.mantenimientos.modules.assets.repository.AssetRepository;
import com.umaso.mantenimientos.modules.locations.entity.Location;
import com.umaso.mantenimientos.modules.locations.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final LocationRepository locationRepository;

    public AssetResponse create(CreateAssetRequest request) {

        if (assetRepository.existsByCodigoInventario(request.codigoInventario())) {
            throw new RuntimeException("El código de inventario ya existe");
        }

        Location location = null;

        if (request.ubicacionId() != null) {
            location = locationRepository.findById(request.ubicacionId())
                    .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));
        }

        Asset asset = Asset.builder()
                .qrUuid(UUID.randomUUID())
                .codigoInventario(request.codigoInventario())
                .nombre(request.nombre())
                .tipo(request.tipo())
                .marca(request.marca())
                .modelo(request.modelo())
                .serialEquipo(request.serialEquipo())
                .ubicacion(location)
                .estado(AssetStatus.ACTIVO)
                .fechaAdquisicion(request.fechaAdquisicion())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Asset savedAsset = assetRepository.save(asset);

        return mapToResponse(savedAsset);
    }

    public List<AssetResponse> findAll() {

        return assetRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AssetResponse findById(UUID id) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        return mapToResponse(asset);
    }

    private AssetResponse mapToResponse(Asset asset) {

        return new AssetResponse(
                asset.getId(),
                asset.getQrUuid(),
                asset.getCodigoInventario(),
                asset.getNombre(),
                asset.getTipo(),
                asset.getMarca(),
                asset.getModelo(),
                asset.getSerialEquipo(),
                asset.getEstado(),
                asset.getFechaAdquisicion(),
                asset.getCreatedAt()
        );
    }
}