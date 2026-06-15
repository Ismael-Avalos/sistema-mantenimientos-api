-- =============================================
-- EXTENSIONES
-- =============================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- TIPOS ENUM
-- =============================================
CREATE TYPE estado_equipo AS ENUM (
    'activo',
    'en_mantenimiento',
    'dado_de_baja'
);

CREATE TYPE tipo_mantenimiento AS ENUM (
    'preventivo',
    'correctivo'
);

-- =============================================
-- TABLA: Roles
-- =============================================
CREATE TABLE Roles (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       nombre VARCHAR(50) UNIQUE NOT NULL,
                       descripcion TEXT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLA: Ubicaciones
-- =============================================
CREATE TABLE Ubicaciones (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             nombre VARCHAR(100) UNIQUE NOT NULL,
                             edificio VARCHAR(100),
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLA: Usuarios
-- =============================================
CREATE TABLE Usuarios (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          nombre VARCHAR(100) NOT NULL,
                          correo VARCHAR(150) UNIQUE NOT NULL,
                          contrasena VARCHAR(255) NOT NULL,
                          rol_id UUID NOT NULL REFERENCES Roles(id) ON DELETE RESTRICT,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLA: Configuración Institucional
-- =============================================
CREATE TABLE Configuracion_Institucional (
                                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                                             nombre_institucion VARCHAR(150) NOT NULL,
                                             sede_principal VARCHAR(100) NOT NULL,

                                             director_ti_nombre VARCHAR(150) NOT NULL,
                                             director_ti_cargo VARCHAR(150) NOT NULL,

                                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLA: Equipos
-- =============================================
CREATE TABLE Equipos (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                         qr_uuid UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),

                         codigo_inventario VARCHAR(50) UNIQUE NOT NULL,

                         nombre VARCHAR(100) NOT NULL,

                         tipo VARCHAR(50) NOT NULL,

                         marca VARCHAR(50),

                         modelo VARCHAR(50),

                         serial_equipo VARCHAR(100) UNIQUE,

                         ubicacion_id UUID REFERENCES Ubicaciones(id) ON DELETE SET NULL,

                         estado estado_equipo NOT NULL DEFAULT 'activo',

                         fecha_adquisicion DATE,

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- SECUENCIA: Número de Reporte
-- =============================================
CREATE SEQUENCE seq_numero_reporte_mantenimiento
    START 1
INCREMENT 1;

-- =============================================
-- TABLA: Mantenimientos
-- =============================================
CREATE TABLE Mantenimientos (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                                numero_reporte BIGINT NOT NULL
                                                    DEFAULT nextval('seq_numero_reporte_mantenimiento'),

                                equipo_id UUID NOT NULL
                                    REFERENCES Equipos(id)
                                        ON DELETE CASCADE,

                                responsable_id UUID
                                                      REFERENCES Usuarios(id)
                                                          ON DELETE SET NULL,

                                tipo tipo_mantenimiento NOT NULL,

                                sede VARCHAR(100) NOT NULL DEFAULT 'Sonsonate',

                                solicitante_nombre VARCHAR(150) NOT NULL,

                                solicitante_correo VARCHAR(150) NOT NULL,

                                solicitante_telefono VARCHAR(30),

                                unidad VARCHAR(150) NOT NULL,

                                descripcion_falla TEXT NOT NULL,

                                actividades_realizadas TEXT NOT NULL,

                                observaciones_tecnicas TEXT,

                                recomendaciones TEXT,

                                costo NUMERIC(12,2) NOT NULL DEFAULT 0,

                                fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                fecha_entrega TIMESTAMP,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT uq_mantenimientos_numero_reporte
                                    UNIQUE (numero_reporte)
);

-- =============================================
-- TABLA: Adjuntos
-- =============================================
CREATE TABLE Adjuntos (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                          mantenimiento_id UUID NOT NULL
                              REFERENCES Mantenimientos(id)
                                  ON DELETE CASCADE,

                          url TEXT NOT NULL,

                          tipo_archivo VARCHAR(50),

                          nombre_original VARCHAR(255),

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- ÍNDICES
-- =============================================
CREATE INDEX idx_mantenimientos_equipo
    ON Mantenimientos(equipo_id);

CREATE INDEX idx_mantenimientos_fecha
    ON Mantenimientos(fecha);

CREATE INDEX idx_mantenimientos_numero_reporte
    ON Mantenimientos(numero_reporte);

CREATE INDEX idx_mantenimientos_sede
    ON Mantenimientos(sede);

CREATE INDEX idx_mantenimientos_fecha_entrega
    ON Mantenimientos(fecha_entrega);

CREATE INDEX idx_equipos_ubicacion
    ON Equipos(ubicacion_id);

CREATE INDEX idx_equipos_estado
    ON Equipos(estado);

CREATE INDEX idx_adjuntos_mantenimiento
    ON Adjuntos(mantenimiento_id);