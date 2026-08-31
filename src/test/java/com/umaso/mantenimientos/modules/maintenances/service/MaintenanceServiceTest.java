package com.umaso.mantenimientos.modules.maintenances.service;

import com.umaso.mantenimientos.modules.assets.entity.Asset;
import com.umaso.mantenimientos.modules.assets.repository.AssetRepository;
import com.umaso.mantenimientos.modules.maintenances.dto.request.UpdateMaintenanceRequest;
import com.umaso.mantenimientos.modules.maintenances.entity.Maintenance;
import com.umaso.mantenimientos.modules.maintenances.entity.MaintenanceType;
import com.umaso.mantenimientos.modules.maintenances.repository.MaintenanceRepository;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MaintenanceServiceTest {
    private MaintenanceRepository maintenances;
    private UserRepository users;
    private MaintenanceService service;
    private Maintenance maintenance;

    @BeforeEach
    void setUp() {
        maintenances = mock(MaintenanceRepository.class);
        users = mock(UserRepository.class);
        service = new MaintenanceService(maintenances, mock(AssetRepository.class), users);

        maintenance = Maintenance.builder()
                .id(UUID.randomUUID())
                .numeroReporte(10L)
                .equipo(Asset.builder().id(UUID.randomUUID()).build())
                .sede("Sonsonate")
                .tipo(MaintenanceType.CORRECTIVO)
                .fecha(LocalDateTime.of(2026, 8, 20, 8, 0))
                .solicitanteNombre("Solicitante")
                .solicitanteCorreo("solicitante@example.com")
                .unidad("Informática")
                .descripcionFalla("Falla")
                .actividadesRealizadas("Diagnóstico")
                .costo(BigDecimal.ZERO)
                .createdAt(LocalDateTime.of(2026, 8, 20, 8, 0))
                .updatedAt(LocalDateTime.of(2026, 8, 20, 8, 0))
                .build();
        when(maintenances.findById(maintenance.getId())).thenReturn(Optional.of(maintenance));
        when(maintenances.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updateChangesAllowedFieldsButKeepsAssetAndSite() {
        Asset originalAsset = maintenance.getEquipo();
        User responsible = User.builder().id(UUID.randomUUID()).nombre("Técnico").build();
        when(users.findById(responsible.getId())).thenReturn(Optional.of(responsible));

        var response = service.actualizarMantenimiento(maintenance.getId(), validUpdate(responsible.getId()));

        assertThat(maintenance.getEquipo()).isSameAs(originalAsset);
        assertThat(maintenance.getSede()).isEqualTo("Sonsonate");
        assertThat(maintenance.getResponsable()).isSameAs(responsible);
        assertThat(response.tipo()).isEqualTo(MaintenanceType.PREVENTIVO);
        assertThat(response.actividadesRealizadas()).isEqualTo("Limpieza y pruebas");
    }

    @Test
    void updateRejectsRequestDateAfterDeliveryDate() {
        UpdateMaintenanceRequest invalid = new UpdateMaintenanceRequest(null, MaintenanceType.PREVENTIVO,
                "Solicitante", "solicitante@example.com", null, "Informática", "Falla",
                "Limpieza y pruebas", null, null, BigDecimal.ZERO,
                LocalDateTime.of(2026, 8, 30, 10, 0), LocalDateTime.of(2026, 8, 29, 10, 0));

        assertThatThrownBy(() -> service.actualizarMantenimiento(maintenance.getId(), invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de solicitud");
        verify(maintenances, never()).save(any());
    }

    @Test
    void deleteExistingMaintenance() {
        service.eliminarMantenimiento(maintenance.getId());
        verify(maintenances).delete(maintenance);
    }

    private UpdateMaintenanceRequest validUpdate(UUID responsibleId) {
        return new UpdateMaintenanceRequest(responsibleId, MaintenanceType.PREVENTIVO,
                "Solicitante actualizado", "nuevo@example.com", "7000-0000", "Informática",
                "Falla actualizada", "Limpieza y pruebas", "Observación", "Recomendación",
                new BigDecimal("15.50"), LocalDateTime.of(2026, 8, 29, 8, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0));
    }
}
