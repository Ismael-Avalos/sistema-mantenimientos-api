package com.umaso.mantenimientos.modules.assets.repository;

import com.umaso.mantenimientos.modules.assets.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findByCodigoInventario(String codigoInventario);

    Optional<Asset> findByQrUuid(UUID qrUuid);

    boolean existsByCodigoInventario(String codigoInventario);

}