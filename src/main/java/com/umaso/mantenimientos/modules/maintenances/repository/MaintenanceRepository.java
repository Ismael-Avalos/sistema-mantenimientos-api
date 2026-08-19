package com.umaso.mantenimientos.modules.maintenances.repository;

import com.umaso.mantenimientos.modules.maintenances.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {

    // Buscar mantenimientos por el UUID del equipo, ordenados del más reciente al más antiguo
    List<Maintenance> findByEquipoIdOrderByFechaDesc(UUID equipoId);

    // En caso de que busques directamente por el qrUuid contenido dentro de la entidad Asset:
    List<Maintenance> findByEquipoQrUuidOrderByFechaDesc(UUID qrUuid);
}