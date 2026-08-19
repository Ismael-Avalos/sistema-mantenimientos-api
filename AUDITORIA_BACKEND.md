# Auditoría del Backend — Gestión de Activos Tecnológicos

**Fecha:** 2026-08-12  
**Alcance:** revisión estática de `src/main`, `src/test` y `pom.xml`. No se modificó código fuente, migraciones ni configuración; este archivo es el único artefacto generado.  
**Dominio evaluado:** inventario de activos con lectura QR mediante UUID inmutable y acceso diferenciado entre Técnico y Administrador.

## Resumen ejecutivo

El backend tiene una estructura modular inicial y los controladores existentes delegan en servicios; sin embargo, **no está listo para un entorno que requiera seguridad por roles ni lectura QR confiable**. El hallazgo principal es que Spring Security permite absolutamente todas las peticiones, el inicio de sesión entrega un token de prueba que no se valida y la respuesta de autenticación expone la entidad `User`. También falla el requisito de UUID QR inmutable: `Asset.qrUuid` tiene setter público generado por Lombok y su columna permite actualizaciones.

Resultado por criterio:

| Criterio | Resultado |
| --- | --- |
| Inglés estricto en código, DTOs, mensajes y comentarios | **No cumple** |
| UUID QR inmutable tras el alta | **No cumple** |
| Controllers sin entidades JPA | **No cumple** (indirectamente en autenticación) |
| Capas Controller → Service → Repository → Entity | **Parcial** |
| Seguridad JWT y Técnico/Admin | **No cumple** |
| Transacciones de lectura/escritura | **Parcial** |
| Sin secretos hardcodeados / deuda técnica | **No cumple** |

---

## Hallazgos de severidad alta

### A-01 — Todos los endpoints son públicos; no existe autorización por roles

**Evidencia:** [`SecurityConfig.java`](src/main/java/com/umaso/mantenimientos/config/SecurityConfig.java) líneas 25–32 desactiva CSRF y establece `.anyRequest().permitAll()`. El proyecto no contiene `@EnableMethodSecurity`, `@PreAuthorize`, `@Secured`, `@RolesAllowed`, filtro `Bearer`/JWT, `OncePerRequestFilter` ni `UserDetailsService`.

**Impacto:** cualquier cliente puede crear usuarios y activos, leer roles, modificar/eliminar ubicaciones y cambiar contraseñas sin autenticarse. No hay distinción efectiva entre Técnico y Administrador.

**Corrección sugerida:** implementar autenticación JWT stateless (`SessionCreationPolicy.STATELESS`), filtro que valide firma, expiración y autoridades; permitir anónimamente solo `POST /api/auth/login`; cerrar el resto por defecto. Activar `@EnableMethodSecurity` y definir una matriz explícita, por ejemplo: `ADMIN` para usuarios, roles y configuración; `ADMIN`/`TECHNICIAN` para consulta de activos y mantenimiento; solamente las operaciones expresamente autorizadas para Técnico. Aplicar `@PreAuthorize("hasRole('ADMIN')")` o reglas equivalentes por endpoint, con pruebas 401/403 para ambos roles.

### A-02 — Autenticación simulada y posible bypass de BCrypt

**Evidencia:** [`AuthService.java`](src/main/java/com/umaso/mantenimientos/modules/auth/service/AuthService.java) líneas 27–30 acepta tanto `passwordEncoder.matches(...)` como comparación de contraseña en texto plano; líneas 40–44 generan `"MOCK-TOKEN-" + UUID.randomUUID()`.

**Impacto:** el token no identifica ni autentica al solicitante y no puede ser verificado posteriormente. La alternativa de texto plano reduce la política de contraseñas al valor almacenado y debe eliminarse antes de producción.

**Corrección sugerida:** aceptar exclusivamente hashes BCrypt/Argon2, migrar de forma controlada los datos antiguos si existen, y emitir JWT firmado con `sub`, `roles`, `iat`, `exp`, `jti`. No retornar ni persistir contraseñas en texto plano. Añadir rate limiting o bloqueo progresivo para intentos fallidos.

### A-03 — La respuesta de login expone una entidad JPA y puede filtrar la contraseña

**Evidencia:** [`AuthResponse.java`](src/main/java/com/umaso/mantenimientos/modules/auth/dto/response/AuthResponse.java) líneas 3 y 15 importa y devuelve `User usuario`; [`AuthController.java`](src/main/java/com/umaso/mantenimientos/modules/auth/controller/AuthController.java) líneas 21–28 devuelve ese DTO. La entidad [`User.java`](src/main/java/com/umaso/mantenimientos/modules/users/entity/User.java) líneas 32–33 contiene `contrasena` sin `@JsonIgnore`.

**Impacto:** incumple la regla de no exponer entidades JPA desde Controllers y puede serializar hash de contraseña, estado interno y relaciones. También puede causar problemas de lazy loading/ciclos de serialización.

**Corrección sugerida:** sustituir `User usuario` por `AuthenticatedUserResponse`/`UserResponse` sin contraseña y sin entidades. Mapear en servicio. Como defensa adicional, marcar el hash con `@JsonIgnore`, pero no usar esto como sustituto del DTO.

### A-04 — El UUID QR de los activos no es inmutable

**Evidencia:** [`Asset.java`](src/main/java/com/umaso/mantenimientos/modules/assets/entity/Asset.java) líneas 13–14 aplica `@Setter` a toda la entidad, generando `setQrUuid(UUID)`; líneas 24–25 definen `qr_uuid` sin `updatable = false`. La migración inicial [`V1__init_schema.sql`](src/main/resources/db/migration/V1__init_schema.sql) línea 74 solo aplica `NOT NULL UNIQUE`, lo que no impide un `UPDATE`.

**Impacto:** un cambio accidental o malicioso de QR rompe la trazabilidad física del activo y permite que una etiqueta QR termine apuntando a un identificador diferente.

**Corrección sugerida:** eliminar el setter del QR (preferiblemente reemplazar `@Setter` global por setters selectivos) y declarar `@Column(name = "qr_uuid", nullable = false, unique = true, updatable = false)`. Generarlo una sola vez en el servicio o con `@PrePersist`; no incluirlo en DTOs de actualización. Añadir una migración nueva que revoque permisos de actualización de esa columna al usuario de aplicación o un trigger PostgreSQL que rechace cambios, y pruebas que demuestren que una actualización no altera el QR. No se deben alterar las migraciones Flyway ya aplicadas.

### A-05 — Credencial hardcodeada en configuración versionable

**Evidencia:** [`application.yaml`](src/main/resources/application.yaml) líneas 15–18 contiene `spring.security.user.password: isma12345`.

**Impacto:** exposición de secreto en el repositorio e inicio potencial con una cuenta administrativa conocida.

**Corrección sugerida:** eliminar el usuario por defecto si la autenticación usa la tabla `usuarios`; si fuera imprescindible para desarrollo, usar una variable de entorno sin valor por defecto y un perfil local no versionado. Rotar inmediatamente la contraseña si el repositorio se ha compartido.

---

## Hallazgos de severidad media

### M-01 — El código no cumple el estándar de inglés estricto

**Veredicto:** **no es 100 % inglés**. Se encontraron identificadores, DTOs, anotaciones de tabla/columna, mensajes y comentarios en español. A continuación se listan las ocurrencias de código relevantes con ruta y líneas exactas; las tablas/columnas existentes requieren una migración de nombres planificada, no un cambio directo de las migraciones históricas.

| Ruta | Elementos en español / spanglish |
| --- | --- |
| [`MantenimientosApplication.java`](src/main/java/com/umaso/mantenimientos/MantenimientosApplication.java) | L1, L7, L10: paquete y clase `mantenimientos`/`MantenimientosApplication`. |
| [`Asset.java`](src/main/java/com/umaso/mantenimientos/modules/assets/entity/Asset.java) | L15 `equipos`; L28, 31, 34, 37, 40, 43, 46, 49, 52, 55: `codigoInventario`, `nombre`, `tipo`, `marca`, `modelo`, `serialEquipo`, `ubicacion`, `estado`, `fechaAdquisicion`, y nombres de columna asociados. |
| [`AssetService.java`](src/main/java/com/umaso/mantenimientos/modules/assets/service/AssetService.java) | L27–28, L33–35, L40–48, L69, L77–98: llamadas, variables y mensajes `codigoInventario`, `ubicacion`, `estado`, `Equipo no encontrado`, etc. |
| [`CreateAssetRequest.java`](src/main/java/com/umaso/mantenimientos/modules/assets/dto/request/CreateAssetRequest.java) y [`AssetResponse.java`](src/main/java/com/umaso/mantenimientos/modules/assets/dto/response/AssetResponse.java) | Campos L12–32 y L16–30: `codigoInventario`, `nombre`, `tipo`, `marca`, `modelo`, `serialEquipo`, `ubicacionId`, `fechaAdquisicion`, `estado`. |
| [`User.java`](src/main/java/com/umaso/mantenimientos/modules/users/entity/User.java) | L15 `usuarios`; L29–46: `nombre`, `correo`, `contrasena`, `rol`, `activo`, `debeCambiarContrasena`, `rol_id`, `debe_cambiar_contrasena`. |
| [`UserService.java`](src/main/java/com/umaso/mantenimientos/modules/users/service/UserService.java) | L27–28, L31–40, L48, L54, L56: nombres, mensajes y comentarios españoles. |
| [`CreateUserRequest.java`](src/main/java/com/umaso/mantenimientos/modules/users/dto/request/CreateUserRequest.java) y [`UserResponse.java`](src/main/java/com/umaso/mantenimientos/modules/users/dto/response/UserResponse.java) | Request L11–20 y mensajes de validación; response L8–13, incluido comentario L13. |
| [`LoginRequest.java`](src/main/java/com/umaso/mantenimientos/modules/auth/dto/request/LoginRequest.java), [`ChangePasswordRequest.java`](src/main/java/com/umaso/mantenimientos/modules/auth/dto/request/ChangePasswordRequest.java), [`AuthService.java`](src/main/java/com/umaso/mantenimientos/modules/auth/service/AuthService.java), [`AuthController.java`](src/main/java/com/umaso/mantenimientos/modules/auth/controller/AuthController.java) | `correo`, `contrasena`, `usuarioId`, `nuevaContrasena`; L25–41 y L54–55 de servicio, y L31, L35 de controller: mensajes/comentarios en español. |
| [`Location.java`](src/main/java/com/umaso/mantenimientos/modules/locations/entity/Location.java), [`LocationService.java`](src/main/java/com/umaso/mantenimientos/modules/locations/service/LocationService.java), [`LocationController.java`](src/main/java/com/umaso/mantenimientos/modules/locations/controller/LocationController.java), DTOs de location | `ubicaciones`, `nombre`, `edificio`; servicio L43–71 y controller L37, L47 tienen comentarios y excepciones españoles. |
| [`Role.java`](src/main/java/com/umaso/mantenimientos/modules/roles/entity/Role.java), [`RoleRepository.java`](src/main/java/com/umaso/mantenimientos/modules/roles/repository/RoleRepository.java), [`RoleResponse.java`](src/main/java/com/umaso/mantenimientos/modules/roles/dto/response/RoleResponse.java) | L23, L26; repositorio L11; DTO L7–8: `nombre`, `descripcion`. |
| [`Maintenance.java`](src/main/java/com/umaso/mantenimientos/modules/maintenances/entity/Maintenance.java) y [`MaintenanceType.java`](src/main/java/com/umaso/mantenimientos/modules/maintenances/entity/MaintenanceType.java) | Tabla/atributos L13, L25–79 y valores L4–5: `mantenimientos`, `numeroReporte`, `equipo`, `responsable`, `tipo`, `sede`, `solicitante*`, `descripcionFalla`, `actividadesRealizadas`, `observacionesTecnicas`, `recomendaciones`, `costo`, `fecha*`, `PREVENTIVO`, `CORRECTIVO`. |
| [`Attachment.java`](src/main/java/com/umaso/mantenimientos/modules/attachments/entity/Attachment.java) y [`InstitutionalConfiguration.java`](src/main/java/com/umaso/mantenimientos/modules/settings/entity/InstitutionalConfiguration.java) | L11, L24–34: `adjuntos`, `mantenimiento`, `tipoArchivo`, `nombreOriginal`; configuración L10 y L22–32: `configuracion_institucional`, `nombreInstitucion`, `sedePrincipal`, `directorTi*`. |
| [`V1__init_schema.sql`](src/main/resources/db/migration/V1__init_schema.sql), [`V2__normalize_enum_colums.sql`](src/main/resources/db/migration/V2__normalize_enum_colums.sql), [`V3__add_usuario_status_fields.sql`](src/main/resources/db/migration/V3__add_usuario_status_fields.sql) | Comentarios, tipos, tablas, columnas, constraints e índices en español: por ejemplo V1 L7–201, V2 L1–29, V3 L2–6. |

**Corrección sugerida:** adoptar un glosario y renombrar por fases: `Asset`, `Location`, `User`, `Role`, `Maintenance`, `Attachment`; `name`, `email`, `passwordHash`, `role`, `active`, `location`, `assetTag`, `maintenanceType`, etc. Convertir mensajes y comentarios a inglés, incluidos los de validación. Para la base de datos, crear migraciones nuevas de renombrado y sincronizar `@Table`/`@Column`; no reescribir V1–V3 en un ambiente con Flyway aplicado.

### M-02 — Transaccionalidad incompleta en AssetService

**Evidencia:** [`AssetService.java`](src/main/java/com/umaso/mantenimientos/modules/assets/service/AssetService.java) líneas 25, 58 y 66 no tienen `@Transactional`; a diferencia de `LocationService`, `UserService`, `RoleService` y `AuthService` que sí lo usan.

**Impacto:** la creación consulta ubicación y guarda el activo sin frontera transaccional explícita; las lecturas mapean una relación `@ManyToOne` potencialmente lazy fuera de una transacción y pueden producir `LazyInitializationException` según la configuración.

**Corrección sugerida:** usar `@Transactional` en `create` y `@Transactional(readOnly = true)` en `findAll`/`findById`; preferir consultas DTO/proyecciones o `fetch join` para ubicación. Mantener transacciones en servicios, no en controllers.

### M-03 — No hay endpoint de lectura por QR pese a que el repositorio lo soporta

**Evidencia:** [`AssetRepository.java`](src/main/java/com/umaso/mantenimientos/modules/assets/repository/AssetRepository.java) línea 13 declara `findByQrUuid`, pero [`AssetService.java`](src/main/java/com/umaso/mantenimientos/modules/assets/service/AssetService.java) y [`AssetController.java`](src/main/java/com/umaso/mantenimientos/modules/assets/controller/AssetController.java) no lo invocan ni exponen ruta de escaneo.

**Impacto:** el flujo central de lectura QR no está implementado desde la API, y las únicas búsquedas expuestas son por `id` interno.

**Corrección sugerida:** incorporar un caso de uso de solo lectura, por ejemplo `GET /maintenances/assets/qr/{qrUuid}`, protegido para `ADMIN` y `TECHNICIAN`, que busque solo por `qrUuid` y retorne `AssetResponse`. Mantener `id` como identificador técnico y documentar el QR como inmutable.

### M-04 — Módulos incompletos y cobertura de pruebas insuficiente

**Evidencia:** `attachments`, `maintenances` y `settings` tienen entidad/repositorio, pero no controller, service ni DTOs; el único test [`MantenimientosApplicationTests.java`](src/test/java/com/umaso/mantenimientos/MantenimientosApplicationTests.java) líneas 6–11 solo carga el contexto.

**Impacto:** la arquitectura es correcta en los flujos ya implementados (Controllers → Services → Repositories; no hay imports de entidad en controllers), pero no puede considerarse cumplimiento estricto del sistema completo. No hay regresiones que aseguren autorización, no exposición de entidad, QR inmutable, ni transacciones.

**Corrección sugerida:** completar cada caso de uso verticalmente con DTOs y servicios, y crear pruebas de integración para matriz de roles, JWT inválido/expirado, serialización sin contraseña, lectura QR y rechazo de actualización del QR.

### M-05 — Excepciones genéricas y tratamiento de errores en controllers

**Evidencia:** se usa `RuntimeException` en [`AssetService.java`](src/main/java/com/umaso/mantenimientos/modules/assets/service/AssetService.java) L28, L35, L69; [`LocationService.java`](src/main/java/com/umaso/mantenimientos/modules/locations/service/LocationService.java) L44, L54, L69; [`AuthService.java`](src/main/java/com/umaso/mantenimientos/modules/auth/service/AuthService.java) L25, L33, L37, L54. [`AuthController.java`](src/main/java/com/umaso/mantenimientos/modules/auth/controller/AuthController.java) L23–28 y L33–38 captura cada una y responde 400.

**Impacto:** no se distinguen 401, 403, 404, 409 y 422; se repite manejo HTTP y se corre el riesgo de revelar mensajes internos.

**Corrección sugerida:** definir excepciones de dominio (`ResourceNotFoundException`, `ConflictException`, `InvalidCredentialsException`) y un `@RestControllerAdvice` que produzca un esquema uniforme de error. El login inválido debe responder 401; recursos inexistentes 404; duplicados 409.

---

## Hallazgos de severidad baja

### B-01 — Validación inconsistente de solicitudes

**Evidencia:** `AssetController` y `UserController` usan `@Valid`, pero [`LocationController.java`](src/main/java/com/umaso/mantenimientos/modules/locations/controller/LocationController.java) líneas 32 y 41 no lo aplica sobre `CreateLocationRequest`, aunque ese record posee restricciones. `AuthController` tampoco valida `LoginRequest` ni `ChangePasswordRequest`; dichos DTOs no tienen constraints.

**Corrección sugerida:** aplicar `@Valid` a todos los cuerpos de solicitud y añadir `@NotBlank`, `@Email`, `@Size` y reglas de contraseña donde correspondan. Validar también que un rol asignado pertenezca al conjunto permitido.

### B-02 — Lombok se usa sin redundancia detectada, pero @Data no es ideal para DTOs sensibles

**Resultado:** no se detectó mezcla de getters/setters explícitos con `@Data` en una misma clase. Las entidades usan `@Getter`/`@Setter`; `LoginRequest`, `ChangePasswordRequest` y `AuthResponse` usan `@Data` sin getters/setters explícitos.

**Mejora sugerida:** para DTOs de entrada/salida preferir `record` o `@Getter` más setters puntuales. Evita `equals`, `hashCode` y `toString` amplios de `@Data` sobre objetos que contienen contraseñas. No es necesario cambiarlo para cumplir el requisito de no redundancia, pero sí es recomendable por seguridad.

### B-03 — No se detectaron impresiones de consola ni catch vacíos

**Resultado:** no se encontraron `System.out.println`, `printStackTrace` ni bloques `catch` vacíos bajo `src`. No hay un logger SLF4J/Logback en los servicios, por lo que los futuros eventos de seguridad/auditoría deben registrarse con `@Slf4j` y sin incluir contraseñas, tokens ni PII innecesaria.

### B-04 — Integridad y concurrencia de datos mejorables

**Evidencia:** [`LocationRepository.java`](src/main/java/com/umaso/mantenimientos/modules/locations/repository/LocationRepository.java) línea 9 tiene `existsByNombre`, pero [`LocationService.java`](src/main/java/com/umaso/mantenimientos/modules/locations/service/LocationService.java) no lo utiliza. `AssetService` verifica `codigoInventario` antes de insertar, pero el resultado puede competir con otra solicitud; la restricción única de base es la protección final.

**Corrección sugerida:** conservar constraints únicos en base, capturar `DataIntegrityViolationException` y convertirla a 409. Establecer timestamps con `@CreationTimestamp`/`@UpdateTimestamp` de manera uniforme o en callbacks de entidad; evitar asignación manual repetida de `LocalDateTime.now()`.

---

## Conformidades verificadas

- Los controllers existentes (`assets`, `locations`, `roles`, `users`, `auth`) dependen de servicios, no de repositorios.
- Salvo el escape indirecto de `User` en `AuthResponse`, los endpoints existentes usan DTOs de request/response; no se hallaron entidades JPA declaradas directamente como parámetros o retornos de los controllers.
- `UserService.findAll`, `LocationService.findAll/findById`, `RoleService.findAll` y `AuthService.login` usan `@Transactional(readOnly = true)`; sus operaciones de escritura tienen `@Transactional`.
- El QR se genera en servidor durante la creación ([`AssetService.java`](src/main/java/com/umaso/mantenimientos/modules/assets/service/AssetService.java) línea 39), no se recibe desde `CreateAssetRequest`.
- Las variables de conexión a BD se externalizan con `DB_URL`, `DB_USERAPI` y `DB_PASSWORDAPI` ([`application.yaml`](src/main/resources/application.yaml) líneas 5–7).

## Plan de refactorización por fases

1. **Fase 0 — Contención:** rotar/eliminar la credencial hardcodeada, bloquear por defecto las rutas y retirar el token mock y el fallback de contraseña en texto plano. Añadir pruebas de que una petición anónima recibe 401/403.
2. **Fase 1 — Identidad y roles:** implementar JWT real, carga de autoridades desde `Role`, matriz ADMIN/TECHNICIAN, filtro de seguridad y pruebas de autorización por endpoint. Deshabilitar el cambio de contraseña por `usuarioId` arbitrario: usar el usuario autenticado o exigir rol ADMIN para restablecimientos.
3. **Fase 2 — Contratos seguros:** reemplazar `AuthResponse.usuario` por DTO seguro; centralizar errores; completar validaciones; añadir el endpoint de resolución QR protegido.
4. **Fase 3 — Integridad QR y transacciones:** hacer `qrUuid` inmutable en Java y PostgreSQL, excluirlo de toda actualización, añadir transacciones a `AssetService` y pruebas de concurrencia/inmutabilidad.
5. **Fase 4 — Normalización y calidad:** ejecutar la migración gradual de inglés para Java, API y BD; completar módulos verticales y aumentar pruebas de integración. Mantener un glosario y una regla de revisión que impida nuevos identificadores/mensajes en español.

