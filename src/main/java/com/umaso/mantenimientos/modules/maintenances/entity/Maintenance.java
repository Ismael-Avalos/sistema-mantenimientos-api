package com.umaso.mantenimientos.modules.maintenances.entity;

import com.umaso.mantenimientos.modules.assets.entity.Asset;
import com.umaso.mantenimientos.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mantenimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Maintenance {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "numero_reporte", nullable = false, unique = true)
    private Long numeroReporte;

    @ManyToOne
    @JoinColumn(name = "equipo_id", nullable = false)
    private Asset equipo;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private User responsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType tipo;

    @Column(nullable = false, length = 100)
    private String sede;

    @Column(name = "solicitante_nombre", nullable = false, length = 150)
    private String solicitanteNombre;

    @Column(name = "solicitante_correo", nullable = false, length = 150)
    private String solicitanteCorreo;

    @Column(name = "solicitante_telefono", length = 30)
    private String solicitanteTelefono;

    @Column(nullable = false, length = 150)
    private String unidad;

    @Column(name = "descripcion_falla", nullable = false, columnDefinition = "TEXT")
    private String descripcionFalla;

    @Column(name = "actividades_realizadas", nullable = false, columnDefinition = "TEXT")
    private String actividadesRealizadas;

    @Column(name = "observaciones_tecnicas", columnDefinition = "TEXT")
    private String observacionesTecnicas;

    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}