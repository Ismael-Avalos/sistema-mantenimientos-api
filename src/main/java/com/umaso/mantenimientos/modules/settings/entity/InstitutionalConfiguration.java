package com.umaso.mantenimientos.modules.settings.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_institucional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionalConfiguration {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "nombre_institucion", nullable = false, length = 150)
    private String nombreInstitucion;

    @Column(name = "sede_principal", nullable = false, length = 100)
    private String sedePrincipal;

    @Column(name = "director_ti_nombre", nullable = false, length = 150)
    private String directorTiNombre;

    @Column(name = "director_ti_cargo", nullable = false, length = 150)
    private String directorTiCargo;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}