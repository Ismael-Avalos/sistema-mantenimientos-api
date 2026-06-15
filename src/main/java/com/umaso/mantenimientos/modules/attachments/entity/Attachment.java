package com.umaso.mantenimientos.modules.attachments.entity;

import com.umaso.mantenimientos.modules.maintenances.entity.Maintenance;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "adjuntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "mantenimiento_id", nullable = false)
    private Maintenance mantenimiento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo;

    @Column(name = "nombre_original", length = 255)
    private String nombreOriginal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}