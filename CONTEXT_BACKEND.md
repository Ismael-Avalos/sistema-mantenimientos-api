# Backend Context — Sistema de Gestión de Activos y Mantenimientos

## 1. Propósito y arquitectura de dominio

API REST para inventariar activos tecnológicos institucionales, ubicarlos y conservar su historial de mantenimiento. El identificador técnico de cada equipo es `id`; `qr_uuid` es el UUID público/inmutable destinado al QR y a futuras consultas móviles.

**Estado actual (~60%)**: CRUD operativo de usuarios (alta/listado), equipos (alta/listado/detalle) y ubicaciones (CRUD); listado de roles; login/cambio de contraseña inicial. Esquema, entidades y repositorios de mantenimientos, adjuntos y configuración institucional ya existen, pero no tienen API/servicio.

**Visión (~40%)**: registrar y consultar mantenimientos preventivos/correctivos por equipo; endpoint de lectura por `qr_uuid`; proteger por rol (ADMIN administra, TECNICO opera mantenimiento); exportar reportes PDF/Excel; sustituir token mock por JWT persistente/renovable apto para cliente móvil/PWA.

Flujo de capas: `Controller -> Service -> Repository (JpaRepository) -> Entity`. Los controladores exponen DTOs en los módulos ya implementados; servicios mapean entidad/DTO y resuelven relaciones.

## 2. Stack y configuración

| Área | Implementación actual |
|---|---|
| Runtime | Java 21; Spring Boot 4.0.6; Maven |
| Web/validación | Spring WebMVC, Bean Validation |
| Persistencia | Spring Data JPA + Hibernate; PostgreSQL; UUID; Flyway |
| Seguridad | Spring Security + BCrypt; CSRF/form/basic desactivados; CORS solo `http://localhost:5173` |
| JWT | **No está implementado ni declarado**. `AuthService` retorna `MOCK-TOKEN-<UUID>` sin filtro, firma ni persistencia. |
| Boilerplate | Lombok (`@Getter/@Setter/@Builder/@RequiredArgsConstructor`) |
| Config | `application.yaml`: `DB_URL`, `DB_USERAPI`, `DB_PASSWORDAPI`; `ddl-auto: validate`; Flyway `baseline-on-migrate: true`. Contiene credenciales Spring Security `admin`/`isma12345`, aunque la cadena deshabilita basic/form login. |

Seguridad efectiva: `SecurityConfig` usa `anyRequest().permitAll()`. Por tanto **todos los endpoints actuales son públicos**, sin autorización por rol. La restricción ADMIN/TECNICO es objetivo pendiente, no comportamiento actual.

## 3. Estructura fuente

```text
com/umaso/mantenimientos/
├── MantenimientosApplication.java
├── config/
│   └── SecurityConfig.java
└── modules/
    ├── auth/            controller, service, dto/request, dto/response
    ├── users/           controller, service, repository, entity, dto/request,response
    ├── assets/          controller, service, repository, entity, dto/request,response
    ├── locations/       controller, service, repository, entity, dto/request,response
    ├── roles/           controller, service, repository, entity, dto/response
    ├── maintenances/    repository, entity (sin controller/service/dto)
    ├── attachments/     repository, entity (sin controller/service/dto)
    └── settings/        repository, entity (sin controller/service/dto)
```

Migraciones: `src/main/resources/db/migration/V1__init_schema.sql`, `V2__normalize_enum_colums.sql`, `V3__add_usuario_status_fields.sql`.

## 4. Esquema y entidades JPA

| Tabla / entidad | Columnas principales y relaciones |
|---|---|
| `roles` / `Role` | `id UUID PK`, `nombre` único, `descripcion`, `created_at`. Un rol referencia muchos usuarios. |
| `usuarios` / `User` | `id`, `nombre`, `correo` único, `contrasena` BCrypt, `rol_id -> roles` (RESTRICT), `activo=true`, `debe_cambiar_contrasena=true`, timestamps. |
| `ubicaciones` / `Location` | `id`, `nombre` único, `edificio`, `created_at`. |
| `equipos` / `Asset` | `id`, **`qr_uuid UUID UNIQUE NOT NULL`**, `codigo_inventario` único, `nombre`, `tipo`, `marca`, `modelo`, `serial_equipo` único, `ubicacion_id -> ubicaciones` (SET NULL), `estado`, `fecha_adquisicion`, timestamps. `AssetService.create` genera `UUID.randomUUID()` para `qrUuid`; no hay endpoint/servicio que lo modifique, aunque la entidad conserva setter. |
| `mantenimientos` / `Maintenance` | `id`, `numero_reporte` único desde secuencia, `equipo_id -> equipos` (CASCADE), `responsable_id -> usuarios` (SET NULL), `tipo`, datos de solicitante/falla/actividades, `costo(12,2)`, fechas y timestamps. |
| `adjuntos` / `Attachment` | `id`, `mantenimiento_id -> mantenimientos` (CASCADE), `url`, `tipo_archivo`, `nombre_original`, `created_at`. |
| `configuracion_institucional` / `InstitutionalConfiguration` | `id`, nombre/sede institucional, nombre/cargo del director TI, `updated_at`. |

Valores persistidos: tras V2, `equipos.estado` es `VARCHAR(20)` con `ACTIVO|EN_MANTENIMIENTO|DADO_DE_BAJA`; `mantenimientos.tipo` es `VARCHAR(20)` con `PREVENTIVO|CORRECTIVO`. Las entidades usan enums `AssetStatus` y `MaintenanceType` con `EnumType.STRING`.

## 5. API REST: contratos actuales y planificados

`Roles actuales` refleja el comportamiento real: `Público` (no hay autenticación/autorización). Entre paréntesis se indica la política objetivo cuando aplica.

| Controller | Método y ruta | Request | Response | Roles actuales / objetivo | Estado |
|---|---|---|---|---|---|
| Auth | `POST /api/auth/login` | `LoginRequest` | `AuthResponse` | Público | Implementado (token mock) |
| Auth | `POST /api/auth/cambiar-contrasena` | `ChangePasswordRequest` | `{message}` | Público / usuario autenticado | Implementado |
| Users | `POST /maintenances/users` | `CreateUserRequest` | `UserResponse` | Público / ADMIN | Implementado |
| Users | `GET /maintenances/users` | — | `List<UserResponse>` | Público / ADMIN | Implementado |
| Assets | `POST /maintenances/assets` | `CreateAssetRequest` | `AssetResponse` | Público / ADMIN | Implementado |
| Assets | `GET /maintenances/assets` | — | `List<AssetResponse>` | Público / ADMIN, TECNICO | Implementado |
| Assets | `GET /maintenances/assets/{id}` | — | `AssetResponse` | Público / ADMIN, TECNICO | Implementado |
| Locations | `GET /maintenances/locations` | — | `List<LocationResponse>` | Público / ADMIN, TECNICO | Implementado |
| Locations | `GET /maintenances/locations/{id}` | — | `LocationResponse` | Público / ADMIN, TECNICO | Implementado |
| Locations | `POST /maintenances/locations` | `CreateLocationRequest` | `LocationResponse` | Público / ADMIN | Implementado |
| Locations | `PUT /maintenances/locations/{id}` | `CreateLocationRequest` | `LocationResponse` | Público / ADMIN | Implementado |
| Locations | `DELETE /maintenances/locations/{id}` | — | vacío (204) | Público / ADMIN | Implementado |
| Roles | `GET /maintenances/roles` | — | `List<RoleResponse>` | Público / ADMIN | Implementado |
| QR (propuesto) | `GET /maintenances/assets/qr/{qrUuid}` | — | `AssetResponse`/vista pública limitada | Público o token QR según política | Pendiente |
| Maintenances (propuesto) | `POST /maintenances` | `CreateMaintenanceRequest` | `MaintenanceResponse` | TECNICO, ADMIN | Pendiente |
| Maintenances (propuesto) | `GET /maintenances`, `GET /maintenances/{id}`, `GET /maintenances/assets/{assetId}` | filtros opcionales | lista/detalle | TECNICO, ADMIN | Pendiente |
| Reports (propuesto) | `GET /maintenances/reports/{id}.pdf`, `GET /maintenances/export.xlsx` | filtros | binario PDF/XLSX | ADMIN, TECNICO | Pendiente |

## 6. DTOs y convenciones

```java
record CreateUserRequest(String nombre, String correo, String contrasena, UUID rolId) {}
record UserResponse(UUID id, String nombre, String correo, String rol,
                    Boolean activo, Boolean debeCambiarContrasena,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {}

record CreateAssetRequest(String codigoInventario, String nombre, String tipo,
                          String marca, String modelo, String serialEquipo,
                          UUID ubicacionId, LocalDate fechaAdquisicion) {}
record AssetResponse(UUID id, UUID qrUuid, String codigoInventario, String nombre,
                     String tipo, String marca, String modelo, String serialEquipo,
                     AssetStatus estado, LocalDate fechaAdquisicion,
                     LocationResponse ubicacion, LocalDateTime createdAt) {}

record CreateLocationRequest(String nombre, String edificio) {}
record LocationResponse(UUID id, String nombre, String edificio, LocalDateTime createdAt) {}
record RoleResponse(UUID id, String nombre, String descripcion) {}
class LoginRequest { String correo; String contrasena; }
class ChangePasswordRequest { UUID usuarioId; String nuevaContrasena; }
class AuthResponse { String token; User usuario; } // mejora: devolver UserResponse, no entidad
```

- Validar `@RequestBody` con `@Valid`; requests actuales validan altas de usuario, equipo y ubicación.
- Aplicar `@Transactional` a escrituras y `@Transactional(readOnly = true)` a consultas (ya usado en Users/Locations/Roles/Auth; falta uniformarlo en Assets y módulos futuros).
- Mantener entidades fuera de respuestas HTTP; mapear a DTOs. Evitar que `AuthResponse` exponga `contrasena` y asociaciones JPA de `User`.
- Centralizar errores con `@RestControllerAdvice` y respuestas de error consistentes; hoy se usan `RuntimeException`/`IllegalArgumentException` y Auth captura localmente.
- Para QR: generar una vez al crear, declarar `qr_uuid` no actualizable (`updatable=false`) y no aceptar ese campo en DTOs de actualización.
- Para seguridad futura: JWT firmado con expiración/refresh, `UserDetails`/filtro, `@PreAuthorize` o reglas por ruta, y contraseñas exclusivamente BCrypt (el login actual admite temporalmente texto plano como fallback).
