package com.umaso.mantenimientos.modules.locations.service;

import com.umaso.mantenimientos.modules.locations.dto.request.CreateLocationRequest;
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
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional
    public LocationResponse create(CreateLocationRequest request) {
        String nombre = request.nombre().trim();
        String edificio = normalizeOptional(request.edificio());
        validateUniqueLocation(nombre, edificio, null);

        Location location = Location.builder()
                .nombre(nombre)
                .edificio(edificio)
                .createdAt(LocalDateTime.now())
                .build();

        Location savedLocation = locationRepository.save(location);

        return mapToResponse(savedLocation);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> findAll() {
        return locationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse findById(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubicación no encontrada con ID: " + id));

        return mapToResponse(location);
    }

    // ACTUALIZAR
    @Transactional
    public LocationResponse update(UUID id, CreateLocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubicación no encontrada con ID: " + id));

        String nombre = request.nombre().trim();
        String edificio = normalizeOptional(request.edificio());
        validateUniqueLocation(nombre, edificio, id);

        location.setNombre(nombre);
        location.setEdificio(edificio);

        // Guardamos los cambios y devolvemos la respuesta mapeada
        Location updatedLocation = locationRepository.save(location);
        return mapToResponse(updatedLocation);
    }

    // ELIMINAR
    @Transactional
    public void delete(UUID id) {
        if (!locationRepository.existsById(id)) {
            throw new RuntimeException("Ubicación no encontrada con ID: " + id);
        }
        locationRepository.deleteById(id);
    }

    private LocationResponse mapToResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getNombre(),
                location.getEdificio(),
                location.getCreatedAt()
        );
    }

    private void validateUniqueLocation(String nombre, String edificio, UUID currentId) {
        boolean alreadyExists = currentId == null
                ? locationRepository.existsByNombreAndEdificio(nombre, edificio)
                : locationRepository.existsByNombreAndEdificioAndIdNot(nombre, edificio, currentId);

        if (alreadyExists) {
            String detail = edificio == null ? nombre : nombre + " - " + edificio;
            throw new IllegalStateException("Ya existe la ubicación: " + detail);
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
