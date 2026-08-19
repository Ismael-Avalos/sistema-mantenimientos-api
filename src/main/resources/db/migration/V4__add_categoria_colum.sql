-- ============================================================
-- Crear extensión UUID si no existe
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- Crear tabla de categorías
-- ============================================================

CREATE TABLE IF NOT EXISTS categorias
(
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT categorias_pkey PRIMARY KEY (id),
    CONSTRAINT categorias_nombre_key UNIQUE (nombre)
    );


-- ============================================================
-- Categoría por defecto para equipos existentes
-- ============================================================

INSERT INTO categorias (nombre, descripcion)
VALUES ('OTROS', 'Categoría para equipos que no pertenecen a una categoría específica')
    ON CONFLICT (nombre) DO NOTHING;


-- ============================================================
-- Agregar categoría a equipos
-- ============================================================

ALTER TABLE equipos
    ADD COLUMN IF NOT EXISTS categoria_id UUID;


-- ============================================================
-- Asignar categoría OTROS a los equipos existentes
-- ============================================================

UPDATE equipos
SET categoria_id = (
    SELECT id
    FROM categorias
    WHERE nombre = 'OTROS'
)
WHERE categoria_id IS NULL;


-- ============================================================
-- Hacer obligatoria la categoría
-- ============================================================

ALTER TABLE equipos
    ALTER COLUMN categoria_id SET NOT NULL;


-- ============================================================
-- Crear llave foránea
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'equipos_categoria_id_fkey'
    ) THEN
ALTER TABLE equipos
    ADD CONSTRAINT equipos_categoria_id_fkey
        FOREIGN KEY (categoria_id)
            REFERENCES categorias (id)
            ON UPDATE NO ACTION
            ON DELETE RESTRICT;
END IF;
END $$;


-- ============================================================
-- Índice para la llave foránea
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_equipos_categoria
    ON equipos (categoria_id);