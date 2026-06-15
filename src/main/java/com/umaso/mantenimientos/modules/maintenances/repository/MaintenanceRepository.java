package com.umaso.mantenimientos.modules.maintenances.repository;

import com.umaso.mantenimientos.modules.maintenances.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
}