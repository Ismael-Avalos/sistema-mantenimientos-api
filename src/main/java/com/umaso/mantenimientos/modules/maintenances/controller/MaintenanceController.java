package com.umaso.mantenimientos.modules.maintenances.controller;

import com.umaso.mantenimientos.modules.maintenances.dto.request.CreateMaintenanceRequest;
import com.umaso.mantenimientos.modules.maintenances.dto.response.MaintenanceResponse;
import com.umaso.mantenimientos.modules.maintenances.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mantenimientos")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // Obtener historial de un equipo por su ID primario
    @GetMapping("/equipo/{equipoId}")
    public ResponseEntity<List<MaintenanceResponse>> obtenerPorEquipo(@PathVariable UUID equipoId) {
        return ResponseEntity.ok(maintenanceService.obtenerHistorialPorEquipoId(equipoId));
    }

    // Obtener historial de un equipo escaneado mediante el UUID del QR
    @GetMapping("/equipo/qr/{qrUuid}")
    public ResponseEntity<List<MaintenanceResponse>> obtenerPorQrUuid(@PathVariable UUID qrUuid) {
        return ResponseEntity.ok(maintenanceService.obtenerHistorialPorQrUuid(qrUuid));
    }

    // Registrar un nuevo mantenimiento
    @PostMapping
    public ResponseEntity<MaintenanceResponse> crear(@Valid @RequestBody CreateMaintenanceRequest request) {
        MaintenanceResponse respuesta = maintenanceService.crearMantenimiento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}