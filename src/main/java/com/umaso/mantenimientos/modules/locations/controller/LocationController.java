package com.umaso.mantenimientos.modules.locations.controller;

import com.umaso.mantenimientos.modules.locations.dto.request.CreateLocationRequest;
import com.umaso.mantenimientos.modules.locations.dto.response.LocationResponse;
import com.umaso.mantenimientos.modules.locations.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/maintenances/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> findAll() {
        return ResponseEntity.ok(locationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(locationService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(@RequestBody CreateLocationRequest request) {
        LocationResponse response = locationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ACTUALIZAR / EDITAR (HTTP 200 OK)
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(
            @PathVariable UUID id,
            @RequestBody CreateLocationRequest request
    ) {
        LocationResponse response = locationService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // ELIMINAR (HTTP 204 NO CONTENT)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}