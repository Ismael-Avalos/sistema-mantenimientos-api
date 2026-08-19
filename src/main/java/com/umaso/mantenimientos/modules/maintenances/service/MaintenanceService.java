package com.umaso.mantenimientos.modules.maintenances.service;

import com.umaso.mantenimientos.modules.assets.entity.Asset;
import com.umaso.mantenimientos.modules.assets.repository.AssetRepository;
import com.umaso.mantenimientos.modules.maintenances.dto.request.CreateMaintenanceRequest;
import com.umaso.mantenimientos.modules.maintenances.dto.response.MaintenanceResponse;
import com.umaso.mantenimientos.modules.maintenances.entity.Maintenance;
import com.umaso.mantenimientos.modules.maintenances.repository.MaintenanceRepository;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> obtenerHistorialPorEquipoId(UUID equipoId) {
        return maintenanceRepository.findByEquipoIdOrderByFechaDesc(equipoId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> obtenerHistorialPorQrUuid(UUID qrUuid) {
        return maintenanceRepository.findByEquipoQrUuidOrderByFechaDesc(qrUuid)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public MaintenanceResponse crearMantenimiento(CreateMaintenanceRequest request) {
        Asset equipo = assetRepository.findById(request.equipoId())
                .orElseThrow(() -> new RuntimeException("El equipo especificado no existe."));

        User responsable = null;
        if (request.responsableId() != null) {
            responsable = userRepository.findById(request.responsableId())
                    .orElse(null);
        }

        LocalDateTime ahora = LocalDateTime.now();

        Maintenance mantenimiento = Maintenance.builder()
                .equipo(equipo)
                .responsable(responsable)
                .tipo(request.tipo())
                .sede(request.sede())
                .solicitanteNombre(request.solicitanteNombre())
                .solicitanteCorreo(request.solicitanteCorreo())
                .solicitanteTelefono(request.solicitanteTelefono())
                .unidad(request.unidad())
                .descripcionFalla(request.descripcionFalla())
                .actividadesRealizadas(request.actividadesRealizadas())
                .observacionesTecnicas(request.observacionesTecnicas())
                .recomendaciones(request.recomendaciones())
                .costo(request.costo())
                .fecha(request.fecha() != null ? request.fecha() : ahora)
                .fechaEntrega(request.fechaEntrega())
                .createdAt(ahora)
                .updatedAt(ahora)
                .build();

        // 1. Guardamos y forzamos la ejecución del SQL INSERT en PostgreSQL
        Maintenance guardado = maintenanceRepository.saveAndFlush(mantenimiento);

        // 2. Leemos la fila directamente de la BD para obtener el numero_reporte generado por la secuencia
        Maintenance registroCompleto = maintenanceRepository.findById(guardado.getId())
                .orElse(guardado);

        return mapToResponse(registroCompleto);
    }

    private MaintenanceResponse mapToResponse(Maintenance m) {
        String nombreResponsable = m.getResponsable() != null
                ? m.getResponsable().getNombre()
                : "Sin asignar";

        return new MaintenanceResponse(
                m.getId(),
                m.getNumeroReporte(),
                m.getTipo(),
                m.getFecha(),
                m.getFechaEntrega(),
                m.getSede(),
                m.getUnidad(),
                m.getSolicitanteNombre(),
                m.getSolicitanteCorreo(),
                m.getSolicitanteTelefono(),
                m.getDescripcionFalla(),
                m.getActividadesRealizadas(),
                m.getObservacionesTecnicas(),
                m.getRecomendaciones(),
                m.getCosto(),
                m.getResponsable() != null ? m.getResponsable().getId() : null,
                nombreResponsable
        );
    }
}