package com.umaso.mantenimientos.modules.maintenances.service;

import com.umaso.mantenimientos.modules.assets.entity.Asset;
import com.umaso.mantenimientos.modules.assets.repository.AssetRepository;
import com.umaso.mantenimientos.modules.maintenances.dto.request.CreateMaintenanceRequest;
import com.umaso.mantenimientos.modules.maintenances.dto.request.UpdateMaintenanceRequest;
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
import java.util.NoSuchElementException;
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

    @Transactional(readOnly = true)
    public MaintenanceResponse obtenerPorId(UUID id) {
        Maintenance mantenimiento = maintenanceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Mantenimiento no encontrado con ID: " + id
                ));

        return mapToResponse(mantenimiento);
    }

    @Transactional
    public MaintenanceResponse crearMantenimiento(CreateMaintenanceRequest request) {
        Asset equipo = assetRepository.findById(request.equipoId())
                .orElseThrow(() -> new NoSuchElementException("El equipo especificado no existe."));

        User responsable = buscarResponsable(request.responsableId());

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fecha = request.fecha() != null ? request.fecha() : ahora;
        validarFechas(fecha, request.fechaEntrega());

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
                .fecha(fecha)
                .fechaEntrega(request.fechaEntrega())
                .createdAt(ahora)
                .updatedAt(ahora)
                .build();

        Maintenance guardado = maintenanceRepository.saveAndFlush(mantenimiento);

        Maintenance registroCompleto = maintenanceRepository.findById(guardado.getId())
                .orElse(guardado);

        return mapToResponse(registroCompleto);
    }

    @Transactional
    public MaintenanceResponse actualizarMantenimiento(UUID id, UpdateMaintenanceRequest request) {
        Maintenance mantenimiento = maintenanceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Mantenimiento no encontrado con ID: " + id));

        validarFechas(request.fecha(), request.fechaEntrega());
        mantenimiento.setResponsable(buscarResponsable(request.responsableId()));
        mantenimiento.setTipo(request.tipo());
        mantenimiento.setSolicitanteNombre(request.solicitanteNombre());
        mantenimiento.setSolicitanteCorreo(request.solicitanteCorreo());
        mantenimiento.setSolicitanteTelefono(request.solicitanteTelefono());
        mantenimiento.setUnidad(request.unidad());
        mantenimiento.setDescripcionFalla(request.descripcionFalla());
        mantenimiento.setActividadesRealizadas(request.actividadesRealizadas());
        mantenimiento.setObservacionesTecnicas(request.observacionesTecnicas());
        mantenimiento.setRecomendaciones(request.recomendaciones());
        mantenimiento.setCosto(request.costo());
        mantenimiento.setFecha(request.fecha());
        mantenimiento.setFechaEntrega(request.fechaEntrega());
        mantenimiento.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(maintenanceRepository.save(mantenimiento));
    }

    @Transactional
    public void eliminarMantenimiento(UUID id) {
        Maintenance mantenimiento = maintenanceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Mantenimiento no encontrado con ID: " + id));
        maintenanceRepository.delete(mantenimiento);
    }

    private User buscarResponsable(UUID responsableId) {
        if (responsableId == null) return null;
        return userRepository.findById(responsableId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Responsable no encontrado con ID: " + responsableId));
    }

    private void validarFechas(LocalDateTime fecha, LocalDateTime fechaEntrega) {
        if (fechaEntrega != null && fecha.isAfter(fechaEntrega)) {
            throw new IllegalArgumentException(
                    "La fecha de solicitud no puede ser posterior a la fecha de entrega.");
        }
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
