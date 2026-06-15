package com.umaso.mantenimientos.modules.locations.repository;

import com.umaso.mantenimientos.modules.locations.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
}