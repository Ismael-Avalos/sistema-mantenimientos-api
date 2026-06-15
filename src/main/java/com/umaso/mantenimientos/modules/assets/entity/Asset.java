package com.umaso.mantenimientos.modules.assets.entity;

import com.umaso.mantenimientos.modules.locations.entity.Location;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "qr_uuid", nullable = false, unique = true)
    private UUID qrUuid;

    @Column(name = "codigo_inventario", nullable = false, unique = true, length = 50)
    private String codigoInventario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(name = "serial_equipo", unique = true, length = 100)
    private String serialEquipo;

    @ManyToOne
    @JoinColumn(name = "ubicacion_id")
    private Location ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private AssetStatus estado;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}