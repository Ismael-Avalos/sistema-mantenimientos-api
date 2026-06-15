ALTER TABLE equipos
    ALTER COLUMN estado DROP DEFAULT;

ALTER TABLE equipos
ALTER COLUMN estado TYPE VARCHAR(20)
    USING UPPER(estado::TEXT);

UPDATE equipos
SET estado = 'ACTIVO'
WHERE estado IS NULL;

ALTER TABLE equipos
    ALTER COLUMN estado SET DEFAULT 'ACTIVO',
ALTER COLUMN estado SET NOT NULL;

ALTER TABLE equipos
    ADD CONSTRAINT chk_equipos_estado
        CHECK (estado IN ('ACTIVO', 'EN_MANTENIMIENTO', 'DADO_DE_BAJA'));

ALTER TABLE mantenimientos
ALTER COLUMN tipo TYPE VARCHAR(20)
    USING UPPER(tipo::TEXT);

ALTER TABLE mantenimientos
    ADD CONSTRAINT chk_mantenimientos_tipo
        CHECK (tipo IN ('PREVENTIVO', 'CORRECTIVO'));

DROP TYPE IF EXISTS estado_equipo;
DROP TYPE IF EXISTS tipo_mantenimiento;