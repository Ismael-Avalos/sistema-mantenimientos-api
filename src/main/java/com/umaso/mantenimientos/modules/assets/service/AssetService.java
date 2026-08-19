package com.umaso.mantenimientos.modules.assets.service;

import com.umaso.mantenimientos.modules.assets.dto.request.CreateAssetRequest;
import com.umaso.mantenimientos.modules.assets.dto.response.AssetResponse;
import com.umaso.mantenimientos.modules.assets.entity.Asset;
import com.umaso.mantenimientos.modules.assets.entity.AssetStatus;
import com.umaso.mantenimientos.modules.assets.repository.AssetRepository;
import com.umaso.mantenimientos.modules.category.dto.response.CategoryResponse;
import com.umaso.mantenimientos.modules.category.entity.Category;
import com.umaso.mantenimientos.modules.category.repository.CategoryRepository;
import com.umaso.mantenimientos.modules.locations.dto.response.LocationResponse;
import com.umaso.mantenimientos.modules.locations.entity.Location;
import com.umaso.mantenimientos.modules.locations.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public AssetResponse create(CreateAssetRequest request) {

        if (assetRepository.existsByCodigoInventario(request.codigoInventario())) {
            throw new RuntimeException("El código de inventario ya existe");
        }

        Location location = null;
        if (request.ubicacionId() != null) {
            location = locationRepository.findById(request.ubicacionId())
                    .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));
        }

        Category category = categoryRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Asset asset = Asset.builder()
                .qrUuid(UUID.randomUUID())
                .codigoInventario(request.codigoInventario())
                .nombre(request.nombre())
                .tipo(request.tipo())
                .marca(request.marca())
                .modelo(request.modelo())
                .serialEquipo(request.serialEquipo())
                .ubicacion(location)
                .categoria(category)
                .estado(request.estado() != null ? request.estado() : AssetStatus.ACTIVO)
                .fechaAdquisicion(request.fechaAdquisicion())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Asset savedAsset = assetRepository.save(asset);

        return mapToResponse(savedAsset);
    }

    @Transactional
    public AssetResponse update(UUID id, CreateAssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        if (!asset.getCodigoInventario().equalsIgnoreCase(request.codigoInventario()) &&
                assetRepository.existsByCodigoInventario(request.codigoInventario())) {
            throw new RuntimeException("El código de inventario ya está registrado en otro equipo");
        }

        Location location = null;
        if (request.ubicacionId() != null) {
            location = locationRepository.findById(request.ubicacionId())
                    .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));
        }

        Category category = categoryRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        asset.setCodigoInventario(request.codigoInventario());
        asset.setNombre(request.nombre());
        asset.setTipo(request.tipo());
        asset.setMarca(request.marca());
        asset.setModelo(request.modelo());
        asset.setSerialEquipo(request.serialEquipo());
        asset.setUbicacion(location);
        asset.setCategoria(category);
        if (request.estado() != null) {
            asset.setEstado(request.estado());
        }
        asset.setFechaAdquisicion(request.fechaAdquisicion());
        asset.setUpdatedAt(LocalDateTime.now());

        Asset updatedAsset = assetRepository.save(asset);

        return mapToResponse(updatedAsset);
    }

    @Transactional
    public void delete(UUID id) {
        if (!assetRepository.existsById(id)) {
            throw new RuntimeException("Equipo no encontrado para eliminar");
        }
        assetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findAll() {
        return assetRepository.findAllWithRelations()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse findById(UUID id) {
        Asset asset = assetRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        return mapToResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssetResponse findByQrUuid(UUID qrUuid) {
        Asset asset = assetRepository.findByQrUuidWithRelations(qrUuid)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado para el código QR enviado"));

        return mapToResponse(asset);
    }

    private AssetResponse mapToResponse(Asset asset) {
        LocationResponse locationDto = null;
        if (asset.getUbicacion() != null) {
            Location loc = asset.getUbicacion();
            locationDto = new LocationResponse(
                    loc.getId(),
                    loc.getNombre(),
                    loc.getEdificio(),
                    loc.getCreatedAt()
            );
        }

        CategoryResponse categoryDto = null;
        if (asset.getCategoria() != null) {
            Category cat = asset.getCategoria();
            categoryDto = new CategoryResponse(
                    cat.getId(),
                    cat.getNombre(),
                    cat.getDescripcion()
            );
        }

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
                locationDto,
                categoryDto,
                asset.getCreatedAt()
        );
    }
}