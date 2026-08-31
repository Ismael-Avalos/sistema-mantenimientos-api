-- Un aula puede repetir su nombre si pertenece a un edificio diferente.
ALTER TABLE ubicaciones
    DROP CONSTRAINT IF EXISTS ubicaciones_nombre_key;

-- COALESCE evita que PostgreSQL permita varias ubicaciones iguales cuando
-- edificio es NULL. El servicio normaliza cadenas vacías a NULL.
CREATE UNIQUE INDEX uq_ubicaciones_nombre_edificio
    ON ubicaciones (nombre, COALESCE(edificio, ''));
