package com.umaso.mantenimientos.modules.assets.repository;

import com.umaso.mantenimientos.modules.assets.entity.Asset;
import com.umaso.mantenimientos.modules.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findByCodigoInventario(String codigoInventario);

    Optional<Asset> findByQrUuid(UUID qrUuid);

    boolean existsByCodigoInventario(String codigoInventario);

    // Obtiene todos los equipos trayendo ubicación y categoría en una sola consulta SQL
    @Query("SELECT a FROM Asset a LEFT JOIN FETCH a.ubicacion LEFT JOIN FETCH a.categoria")
    List<Asset> findAllWithRelations();

    // Obtiene un equipo por ID trayendo ubicación y categoría en una sola consulta SQL
    @Query("SELECT a FROM Asset a LEFT JOIN FETCH a.ubicacion LEFT JOIN FETCH a.categoria WHERE a.id = :id")
    Optional<Asset> findByIdWithRelations(@Param("id") UUID id);

    // Obtiene un equipo por QR UUID trayendo ubicación y categoría en una sola consulta SQL
    @Query("SELECT a FROM Asset a LEFT JOIN FETCH a.ubicacion LEFT JOIN FETCH a.categoria WHERE a.qrUuid = :qrUuid")
    Optional<Asset> findByQrUuidWithRelations(@Param("qrUuid") UUID qrUuid);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Asset a SET a.categoria = :targetCategory WHERE a.categoria.id = :sourceId")
    void reassignCategory(@Param("sourceId") UUID sourceId, @Param("targetCategory") Category targetCategory);

    long countByCategoriaId(UUID categoriaId);
}