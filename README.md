# Sistema de Mantenimientos y Gestión de Activos Tecnológicos - API REST

Una API REST robusta desarrollada con **Java 21** y **Spring Boot 4.0.6** para inventariar, ubicar y mantener un historial completo de mantenimiento de activos tecnológicos en instituciones educativas.

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Características](#características)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Requisitos](#requisitos)
- [Configuración del Ambiente](#configuración-del-ambiente)
- [Instalación](#instalación)
- [Ejecución](#ejecución)
- [Documentación de API](#documentación-de-api)
- [Autenticación y Seguridad](#autenticación-y-seguridad)
- [Base de Datos](#base-de-datos)
- [Migraciones](#migraciones)
- [Notas de Desarrollo](#notas-de-desarrollo)

## Descripción General

### ¿Qué es el Sistema de Mantenimientos?

Una plataforma centralizada diseñada para instituciones educativas que necesitan gestionar su inventario de equipos tecnológicos de forma eficiente y mantener un registro detallado de todos los mantenimientos realizados.

### Propósito

- **Inventariar** equipos tecnológicos institucionales con identificadores únicos (QR UUID)
- **Ubicar** y rastrear la localización física de cada activo
- **Registrar** y consultar el historial completo de mantenimiento (preventivo y correctivo)
- **Generar reportes** de mantenimiento con datos técnicos y administrativos
- **Gestionar usuarios** y sus roles dentro de la institución

### Problema que Resuelve

Las instituciones sin un sistema centralizado enfrentan:
- Pérdida de control sobre activos tecnológicos
- Imposibilidad de auditar historial de mantenimiento
- Dificultad para planificar mantenimiento preventivo
- Falta de trazabilidad de costos de reparación

## Características

### Módulos Implementados

#### 1. **Autenticación**
- Login con correo y contraseña
- Cambio de contraseña (con bandera de cambio obligatorio)
- Generación de tokens para sesiones
- Contraseñas cifradas con BCrypt

#### 2. **Gestión de Usuarios**
- Crear nuevos usuarios con asignación de rol
- Listar todos los usuarios
- Estados de usuario (activo/inactivo)
- Bandera de cambio de contraseña obligatorio en primer login

#### 3. **Gestión de Activos**
- Crear equipos con identificador QR automático (UUID único)
- Consultar lista completa de equipos
- Consultar detalle de equipo por ID
- Buscar equipos por UUID QR
- Actualizar información de equipos
- Eliminar equipos
- Estados de equipos: Activo, En Mantenimiento, Dado de Baja
- Seguimiento de ubicación por equipo
- Categorización de equipos

#### 4. **Gestión de Ubicaciones**
- CRUD completo de ubicaciones (salas, edificios, pisos)
- Listar ubicaciones disponibles
- Consultar ubicación por ID
- Crear, actualizar y eliminar ubicaciones

#### 5. **Gestión de Roles**
- Listar roles disponibles en el sistema
- Roles base: ADMIN, TÉCNICO, usuarios estándar

#### 6. **Base de Datos - Entidades Disponibles**
Las siguientes entidades están modeladas en la base de datos para funcionalidad futura:
- **Mantenimientos**: Registro de mantenimientos (preventivo/correctivo) con número de reporte, responsable, descripción de falla, actividades realizadas
- **Adjuntos**: Documentos asociados a mantenimientos
- **Configuración Institucional**: Datos de la institución y director de TI

## Stack Tecnológico

| Componente | Versión/Tecnología |
|---|---|
| **Runtime** | Java 21 |
| **Framework** | Spring Boot 4.0.6 |
| **Build** | Maven 3.x (Maven Wrapper incluido) |
| **Persistencia** | Spring Data JPA + Hibernate |
| **Base de Datos** | PostgreSQL 12+ |
| **Migraciones** | Flyway |
| **Seguridad** | Spring Security + BCrypt |
| **Validación** | Jakarta Bean Validation |
| **Web** | Spring Web MVC |
| **Boilerplate** | Lombok |

## Arquitectura

El backend sigue una arquitectura por capas modularizada:

```
┌─────────────────────────────────────────────┐
│          Controllers (REST API)              │
│  - AuthController                            │
│  - UserController                            │
│  - AssetController                           │
│  - LocationController                        │
│  - RoleController                            │
└────────────────┬────────────────────────────┘
                 │
┌─────────────────▼────────────────────────────┐
│          Services (Business Logic)            │
│  - AuthService                                │
│  - UserService                                │
│  - AssetService                               │
│  - LocationService                            │
│  - RoleService                                │
└────────────────┬────────────────────────────┘
                 │
┌─────────────────▼────────────────────────────┐
│        Repositories (Data Access)             │
│  - UserRepository                             │
│  - AssetRepository                            │
│  - LocationRepository                         │
│  - RoleRepository                             │
│  - MaintenanceRepository                      │
│  - AttachmentRepository                       │
│  - CategoryRepository                         │
│  - InstitutionalConfigurationRepository       │
└────────────────┬────────────────────────────┘
                 │
┌─────────────────▼────────────────────────────┐
│      Entities (JPA / Hibernate)               │
│  - User, Role, Location, Asset                │
│  - Maintenance, Attachment, Category          │
│  - InstitutionalConfiguration                 │
└─────────────────────────────────────────────┘
                 │
┌─────────────────▼────────────────────────────┐
│      PostgreSQL Database                      │
└─────────────────────────────────────────────┘
```

### Flujo de Datos

**Petición HTTP** → **Controller** → **Service** → **Repository** → **Database**

### Capas

- **Controllers**: Exponen endpoints REST, validan requests, invocan servicios
- **Services**: Implementan lógica de negocio, mapeo entre DTOs y entidades, transacciones
- **Repositories**: Acceso a datos mediante Spring Data JPA
- **Entities**: Modelos persistidos en PostgreSQL
- **DTOs**: Contracts de entrada/salida (request/response)
- **Configuration**: Configuración de seguridad, CORS, codificadores de contraseña
- **Shared**: Componentes compartidos (excepciones, utilidades)

## Estructura del Proyecto

```
src/main/
├── java/com/umaso/mantenimientos/
│   ├── MantenimientosApplication.java          # Punto de entrada Spring Boot
│   ├── config/
│   │   └── SecurityConfig.java                 # Configuración de Spring Security
│   ├── modules/
│   │   ├── auth/
│   │   │   ├── controller/                     # AuthController
│   │   │   ├── service/                        # AuthService
│   │   │   └── dto/
│   │   │       ├── request/                    # LoginRequest, ChangePasswordRequest
│   │   │       └── response/                   # AuthResponse
│   │   ├── users/
│   │   │   ├── controller/                     # UserController
│   │   │   ├── service/                        # UserService
│   │   │   ├── repository/                     # UserRepository
│   │   │   ├── entity/                         # User
│   │   │   └── dto/
│   │   │       ├── request/                    # CreateUserRequest
│   │   │       └── response/                   # UserResponse
│   │   ├── assets/
│   │   │   ├── controller/                     # AssetController
│   │   │   ├── service/                        # AssetService
│   │   │   ├── repository/                     # AssetRepository
│   │   │   ├── entity/                         # Asset
│   │   │   └── dto/
│   │   │       ├── request/                    # CreateAssetRequest
│   │   │       └── response/                   # AssetResponse
│   │   ├── locations/
│   │   │   ├── controller/                     # LocationController
│   │   │   ├── service/                        # LocationService
│   │   │   ├── repository/                     # LocationRepository
│   │   │   ├── entity/                         # Location
│   │   │   └── dto/
│   │   │       ├── request/                    # CreateLocationRequest
│   │   │       └── response/                   # LocationResponse
│   │   ├── roles/
│   │   │   ├── controller/                     # RoleController
│   │   │   ├── service/                        # RoleService
│   │   │   ├── repository/                     # RoleRepository
│   │   │   ├── entity/                         # Role
│   │   │   └── dto/response/                   # RoleResponse
│   │   ├── maintenances/
│   │   │   ├── repository/                     # MaintenanceRepository
│   │   │   └── entity/                         # Maintenance
│   │   ├── attachments/
│   │   │   ├── repository/                     # AttachmentRepository
│   │   │   └── entity/                         # Attachment
│   │   ├── category/
│   │   │   ├── repository/                     # CategoryRepository
│   │   │   └── entity/                         # Category
│   │   └── settings/
│   │       ├── repository/                     # InstitutionalConfigurationRepository
│   │       └── entity/                         # InstitutionalConfiguration
│   └── shared/
│       └── exception/                          # Manejo centralizado de excepciones
├── resources/
│   ├── application.yaml                        # Configuración de Spring Boot
│   └── db/migration/
│       ├── V1__init_schema.sql                 # Esquema inicial
│       ├── V2__normalize_enum_colums.sql       # Normalización de enums
│       ├── V3__add_usuario_status_fields.sql   # Campos de estado de usuario
│       └── V4__add_categoria_colum.sql         # Categorías de equipos
└── test/
    └── java/                                    # (Pruebas unitarias/integración)
```

## Requisitos

### Sistema

- **Java 21** (JDK 21)
- **PostgreSQL 12+**
- **Maven 3.6+** (incluye Maven Wrapper)
- **Git**

### Herramientas Opcionales

- IDE: IntelliJ IDEA, Eclipse, Visual Studio Code
- Cliente REST: Postman, Insomnia, Thunder Client
- Gestor de BD: pgAdmin, DBeaver

## Configuración del Ambiente

### 1. Variables de Entorno Requeridas

El archivo `src/main/resources/application.yaml` usa las siguientes variables de entorno:

```bash
# Conexión a PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/sistema_mantenimientos
DB_USERAPI=postgres
DB_PASSWORDAPI=your_database_password
```

**Ejemplo de valores seguros para desarrollo local:**

```bash
DB_URL=jdbc:postgresql://localhost:5432/sistema_mantenimientos
DB_USERAPI=postgres
DB_PASSWORDAPI=dev_password_123
```

⚠️ **Nota:** Nunca commit contraseñas reales en el repositorio. Usa archivos `.env` locales o secretos de CI/CD en producción.

### 2. Crear la Base de Datos en PostgreSQL

```bash
psql -U postgres

CREATE DATABASE sistema_mantenimientos OWNER postgres;
```

### 3. Configurar Variables de Entorno

**Linux/macOS:**

```bash
export DB_URL=jdbc:postgresql://localhost:5432/sistema_mantenimientos
export DB_USERAPI=postgres
export DB_PASSWORDAPI=your_database_password
```

**Windows (CMD):**

```cmd
set DB_URL=jdbc:postgresql://localhost:5432/sistema_mantenimientos
set DB_USERAPI=postgres
set DB_PASSWORDAPI=your_database_password
```

**Windows (PowerShell):**

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/sistema_mantenimientos"
$env:DB_USERAPI = "postgres"
$env:DB_PASSWORDAPI = "your_database_password"
```

## Instalación

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/Ismael-Avalos/sistema-mantenimientos-api.git
cd sistema-mantenimientos-api
```

### Paso 2: Verificar Java 21

```bash
java -version
```

Debe mostrar Java 21.x. Si no está instalado, descargalo desde [oracle.com](https://www.oracle.com/java/technologies/downloads/) o usa OpenJDK:

```bash
# macOS con Homebrew
brew install openjdk@21

# O descarga el JDK
```

### Paso 3: Crear la Base de Datos PostgreSQL

```bash
# Conectarse a PostgreSQL
psql -U postgres

# Crear la base de datos
CREATE DATABASE sistema_mantenimientos OWNER postgres;

# Salir
\q
```

### Paso 4: Configurar Variables de Entorno

Ver sección [Configuración del Ambiente](#configuración-del-ambiente).

### Paso 5: Compilar el Proyecto

```bash
./mvnw clean compile
```

O en Windows:

```cmd
mvnw.cmd clean compile
```

### Paso 6: Ejecutar Migraciones (Flyway)

Las migraciones se ejecutan automáticamente al iniciar la aplicación. Alternativamente, puedes ejecutarlas manualmente:

```bash
./mvnw flyway:migrate
```

## Ejecución

### Opción 1: Spring Boot Maven Plugin (Desarrollo)

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

### Opción 2: Compilar y Ejecutar JAR

```bash
# Compilar y empaquetar
./mvnw clean package

# Ejecutar el JAR generado
java -jar target/mantenimientos-0.0.1-SNAPSHOT.jar
```

### Opción 3: Solo Compilar

```bash
./mvnw clean package -DskipTests
```

### Verificar que la Aplicación Está Corriendo

```bash
curl http://localhost:8080/maintenances/roles

# O en el navegador
http://localhost:8080/maintenances/roles
```

## Documentación de API

### Autenticación

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "correo": "usuario@example.com",
  "contrasena": "password123"
}
```

**Respuesta (200 OK):**

```json
{
  "token": "MOCK-TOKEN-550e8400-e29b-41d4-a716-446655440000",
  "usuario": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Admin User",
    "correo": "admin@example.com",
    "rol": "ADMIN",
    "activo": true,
    "debeCambiarContrasena": false,
    "createdAt": "2026-08-25T12:00:00",
    "updatedAt": "2026-08-25T12:00:00"
  }
}
```

#### Cambiar Contraseña

```http
POST /api/auth/cambiar-contrasena
Content-Type: application/json

{
  "usuarioId": "550e8400-e29b-41d4-a716-446655440000",
  "nuevaContrasena": "newpassword456"
}
```

**Respuesta (200 OK):**

```json
{
  "message": "Contraseña actualizada exitosamente"
}
```

### Usuarios

#### Crear Usuario

```http
POST /maintenances/users
Content-Type: application/json

{
  "nombre": "Juan Pérez",
  "correo": "juan@example.com",
  "contrasena": "password123",
  "rolId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Respuesta (200 OK):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "nombre": "Juan Pérez",
  "correo": "juan@example.com",
  "rol": "ADMIN",
  "activo": true,
  "debeCambiarContrasena": true,
  "createdAt": "2026-08-25T12:00:00",
  "updatedAt": "2026-08-25T12:00:00"
}
```

#### Listar Usuarios

```http
GET /maintenances/users
```

**Respuesta (200 OK):** Lista de usuarios

### Activos

#### Crear Activo

```http
POST /maintenances/assets
Content-Type: application/json

{
  "codigoInventario": "TECH-001",
  "nombre": "Monitor Dell 24\"",
  "tipo": "Monitor",
  "marca": "Dell",
  "modelo": "S2421H",
  "serialEquipo": "SN123456789",
  "ubicacionId": "550e8400-e29b-41d4-a716-446655440000",
  "fechaAdquisicion": "2024-01-15"
}
```

**Respuesta (201 CREATED):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "qrUuid": "a1b2c3d4-e5f6-4789-abcd-ef1234567890",
  "codigoInventario": "TECH-001",
  "nombre": "Monitor Dell 24\"",
  "tipo": "Monitor",
  "marca": "Dell",
  "modelo": "S2421H",
  "serialEquipo": "SN123456789",
  "estado": "ACTIVO",
  "fechaAdquisicion": "2024-01-15",
  "ubicacion": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Sala de Servidores",
    "edificio": "A",
    "createdAt": "2026-08-25T12:00:00"
  },
  "createdAt": "2026-08-25T12:00:00"
}
```

#### Listar Activos

```http
GET /maintenances/assets
```

#### Obtener Activo por ID

```http
GET /maintenances/assets/{id}
```

#### Buscar Activo por QR UUID

```http
GET /maintenances/assets/qr/{qrUuid}
```

#### Actualizar Activo

```http
PUT /maintenances/assets/{id}
Content-Type: application/json

{
  "codigoInventario": "TECH-001",
  "nombre": "Monitor Dell 27\"",
  ...
}
```

#### Eliminar Activo

```http
DELETE /maintenances/assets/{id}
```

**Respuesta (204 NO CONTENT)**

### Ubicaciones

#### Listar Ubicaciones

```http
GET /maintenances/locations
```

#### Obtener Ubicación por ID

```http
GET /maintenances/locations/{id}
```

#### Crear Ubicación

```http
POST /maintenances/locations
Content-Type: application/json

{
  "nombre": "Sala de Servidores",
  "edificio": "Edificio A"
}
```

**Respuesta (201 CREATED):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Sala de Servidores",
  "edificio": "Edificio A",
  "createdAt": "2026-08-25T12:00:00"
}
```

#### Actualizar Ubicación

```http
PUT /maintenances/locations/{id}
Content-Type: application/json

{
  "nombre": "Sala de Servidores Principal",
  "edificio": "Edificio A"
}
```

#### Eliminar Ubicación

```http
DELETE /maintenances/locations/{id}
```

**Respuesta (204 NO CONTENT)**

### Roles

#### Listar Roles

```http
GET /maintenances/roles
```

**Respuesta (200 OK):**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "ADMIN",
    "descripcion": "Administrador del Sistema"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "nombre": "TECNICO",
    "descripcion": "Técnico de Mantenimiento"
  }
]
```

## Autenticación y Seguridad

### Estado Actual

- **Spring Security** está configurado pero todos los endpoints son públicos (`permitAll()`)
- **CORS** habilitado solo para `http://localhost:5173` (frontend local)
- **Contraseñas** cifradas con BCrypt
- **CSRF** deshabilitado (API REST, no formularios)
- **Form Login y HTTP Basic** deshabilitados

### Autenticación

- **Login**: Por correo y contraseña
- **Tokens**: Generados como `MOCK-TOKEN-<UUID>` (sin firma ni expiración)
- **Cambio de Contraseña**: Disponible con bandera `debeCambiarContrasena`

### Seguridad Futura

Está planeado implementar:
- Autenticación JWT con firma y expiración
- Autorización basada en roles (`@PreAuthorize`)
- Filtro de autenticación personalizado
- Refresh tokens
- Rate limiting

## Base de Datos

### Tecnología

- **Motor**: PostgreSQL 12+
- **ORM**: Hibernate (Spring Data JPA)
- **Driver**: postgresql-42.x

### Configuración JPA

```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate  # Valida pero no modifica esquema
  flyway:
    baseline-on-migrate: true
```

### Conexión

Las migraciones se ejecutan automáticamente al iniciar la aplicación si el esquema no existe.

## Migraciones

Flyway gestiona el versionamiento de la base de datos. Las migraciones están en `src/main/resources/db/migration/`

### Migraciones Disponibles

| Versión | Nombre | Descripción |
|---|---|---|
| V1 | `init_schema.sql` | Esquema inicial: tablas de roles, usuarios, ubicaciones, equipos, mantenimientos, adjuntos |
| V2 | `normalize_enum_colums.sql` | Normalización de columnas enum |
| V3 | `add_usuario_status_fields.sql` | Campos de estado y cambio de contraseña en usuarios |
| V4 | `add_categoria_colum.sql` | Tabla de categorías y relación con equipos |

### Entidades Principales

#### Roles

- `id` (UUID, PK)
- `nombre` (VARCHAR, UNIQUE)
- `descripcion` (TEXT)
- `created_at` (TIMESTAMP)

#### Usuarios

- `id` (UUID, PK)
- `nombre` (VARCHAR)
- `correo` (VARCHAR, UNIQUE)
- `contrasena` (VARCHAR, BCrypt)
- `rol_id` (FK → Roles)
- `activo` (BOOLEAN)
- `debe_cambiar_contrasena` (BOOLEAN)
- `created_at`, `updated_at` (TIMESTAMP)

#### Ubicaciones

- `id` (UUID, PK)
- `nombre` (VARCHAR, UNIQUE)
- `edificio` (VARCHAR)
- `created_at` (TIMESTAMP)

#### Equipos

- `id` (UUID, PK)
- `qr_uuid` (UUID, UNIQUE) — Identificador público QR
- `codigo_inventario` (VARCHAR, UNIQUE)
- `nombre` (VARCHAR)
- `tipo` (VARCHAR)
- `marca` (VARCHAR)
- `modelo` (VARCHAR)
- `serial_equipo` (VARCHAR, UNIQUE)
- `ubicacion_id` (FK → Ubicaciones)
- `categoria_id` (FK → Categorias)
- `estado` (VARCHAR: ACTIVO, EN_MANTENIMIENTO, DADO_DE_BAJA)
- `fecha_adquisicion` (DATE)
- `created_at`, `updated_at` (TIMESTAMP)

#### Mantenimientos

- `id` (UUID, PK)
- `numero_reporte` (BIGINT, UNIQUE, auto-generated)
- `equipo_id` (FK → Equipos, CASCADE)
- `responsable_id` (FK → Usuarios, SET NULL)
- `tipo` (VARCHAR: PREVENTIVO, CORRECTIVO)
- `solicitante_nombre`, `correo`, `telefono` (VARCHAR)
- `unidad` (VARCHAR)
- `descripcion_falla` (TEXT)
- `actividades_realizadas` (TEXT)
- `observaciones_tecnicas` (TEXT)
- `recomendaciones` (TEXT)
- `costo` (NUMERIC)
- `fecha`, `fecha_entrega` (TIMESTAMP)
- `created_at`, `updated_at` (TIMESTAMP)

#### Adjuntos

- `id` (UUID, PK)
- `mantenimiento_id` (FK → Mantenimientos, CASCADE)
- `url` (TEXT)
- `tipo_archivo` (VARCHAR)
- `nombre_original` (VARCHAR)
- `created_at` (TIMESTAMP)

#### Categorías

- `id` (UUID, PK)
- `nombre` (VARCHAR, UNIQUE)
- `descripcion` (VARCHAR)
- `created_at`, `updated_at` (TIMESTAMP)

### Crear Nuevas Migraciones

1. Crear archivo SQL en `src/main/resources/db/migration/`
2. Nombrar como `V<N>__<descripcion>.sql` (e.g., `V5__add_new_table.sql`)
3. Reiniciar la aplicación; Flyway ejecutará automáticamente

## Notas de Desarrollo

### DTOs y Convenciones

El proyecto usa:
- **Records** para DTOs simples (cuando es posible)
- **Clases** para DTOs más complejos
- Separación en `request` y `response` packages
- Validación con `@Valid` en controllers
- Mapeo manual entre entidades y DTOs

### Transacciones

- `@Transactional` en servicios para escritura
- `@Transactional(readOnly = true)` para consultas

### Validación

- `@Valid` en `@RequestBody`
- Anotaciones de Jakarta Validation (`@NotNull`, `@NotBlank`, etc.)

### UUID Automáticos

- Generados en base de datos con `uuid_generate_v4()`
- El campo `qr_uuid` en equipos es único y no actualizable

### Manejo de Errores

- `RuntimeException` y `IllegalArgumentException` para errores de negocio
- Local try-catch en AuthController
- Mejora futura: `@RestControllerAdvice` centralizado

### CORS

- Configurado para `http://localhost:5173` (frontend local)
- Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS, PATCH
- Credenciales habilitadas

