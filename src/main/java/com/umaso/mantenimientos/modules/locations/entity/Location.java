package com.umaso.mantenimientos.modules.locations.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ubicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 100)
    private String edificio;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}