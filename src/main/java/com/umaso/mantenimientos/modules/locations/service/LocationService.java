package com.umaso.mantenimientos.modules.locations.service;

import com.umaso.mantenimientos.modules.locations.dto.CreateLocationRequest;
import com.umaso.mantenimientos.modules.locations.dto.LocationResponse;
import com.umaso.mantenimientos.modules.locations.entity.Location;
import com.umaso.mantenimientos.modules.locations.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationResponse create(CreateLocationRequest request) {
        Location location = Location.builder()
                .nombre(request.nombre())
                .edificio(request.edificio())
                .createdAt(LocalDateTime.now())
                .build();

        Location savedLocation = locationRepository.save(location);

        return mapToResponse(savedLocation);
    }

    public List<LocationResponse> findAll() {
        return locationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LocationResponse findById(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));

        return mapToResponse(location);
    }

    private LocationResponse mapToResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getNombre(),
                location.getEdificio(),
                location.getCreatedAt()
        );
    }
}