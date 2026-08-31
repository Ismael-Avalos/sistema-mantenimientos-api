package com.umaso.mantenimientos.modules.locations.repository;

import com.umaso.mantenimientos.modules.locations.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    boolean existsByNombreAndEdificio(String nombre, String edificio);

    boolean existsByNombreAndEdificioAndIdNot(String nombre, String edificio, UUID id);
}
