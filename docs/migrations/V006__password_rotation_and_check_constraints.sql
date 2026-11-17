-- Upgrade: password rotation support and data-range CHECK constraints.
-- MySQL 8.0 compatible and idempotent (information_schema guards + PREPARE).
-- Execute once, after selecting erp_little_horizons.

-- ----------------------------------------------------------------------------
-- 1. Password rotation timestamp (usuarios).
--    Legacy rows inherit their creation date so they are not forced to
--    rotate immediately.
-- ----------------------------------------------------------------------------
SET @has_password_changed_at := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'password_changed_at'
);
SET @add_password_changed_at := IF(
    @has_password_changed_at > 0,
    'SELECT 1',
    'ALTER TABLE usuarios ADD COLUMN password_changed_at TIMESTAMP NULL'
);
PREPARE add_password_changed_at_statement FROM @add_password_changed_at;
EXECUTE add_password_changed_at_statement;
DEALLOCATE PREPARE add_password_changed_at_statement;

UPDATE usuarios
SET password_changed_at = fecha_creacion
WHERE password_changed_at IS NULL;

-- ----------------------------------------------------------------------------
-- 2. Data-range CHECK constraints (static bounds only: MySQL 8.0 does not
--    allow non-deterministic functions such as CURRENT_DATE in CHECKs).
-- ----------------------------------------------------------------------------
SET @add_birth_date_check := IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'estudiantes'
       AND CONSTRAINT_TYPE = 'CHECK'
       AND CONSTRAINT_NAME = 'chk_estudiantes_nacimiento') > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE estudiantes ADD CONSTRAINT chk_estudiantes_nacimiento ',
        'CHECK (fecha_nacimiento >= ''1900-01-01'')')
);
PREPARE add_birth_date_check_statement FROM @add_birth_date_check;
EXECUTE add_birth_date_check_statement;
DEALLOCATE PREPARE add_birth_date_check_statement;

SET @add_matriculas_year_check := IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'matriculas'
       AND CONSTRAINT_TYPE = 'CHECK'
       AND CONSTRAINT_NAME = 'chk_matriculas_anio') > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE matriculas ADD CONSTRAINT chk_matriculas_anio ',
        'CHECK (año_academico BETWEEN 2000 AND 2100)')
);
PREPARE add_matriculas_year_check_statement FROM @add_matriculas_year_check;
EXECUTE add_matriculas_year_check_statement;
DEALLOCATE PREPARE add_matriculas_year_check_statement;

SET @add_asignaciones_year_check := IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'asignaciones_profesor'
       AND CONSTRAINT_TYPE = 'CHECK'
       AND CONSTRAINT_NAME = 'chk_asignaciones_anio') > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE asignaciones_profesor ADD CONSTRAINT chk_asignaciones_anio ',
        'CHECK (año_academico BETWEEN 2000 AND 2100)')
);
PREPARE add_asignaciones_year_check_statement FROM @add_asignaciones_year_check;
EXECUTE add_asignaciones_year_check_statement;
DEALLOCATE PREPARE add_asignaciones_year_check_statement;

SET @add_inscriptos_year_check := IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'inscripciones_materia'
       AND CONSTRAINT_TYPE = 'CHECK'
       AND CONSTRAINT_NAME = 'chk_inscriptos_anio') > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE inscripciones_materia ADD CONSTRAINT chk_inscriptos_anio ',
        'CHECK (año_academico BETWEEN 2000 AND 2100)')
);
PREPARE add_inscriptos_year_check_statement FROM @add_inscriptos_year_check;
EXECUTE add_inscriptos_year_check_statement;
DEALLOCATE PREPARE add_inscriptos_year_check_statement;

SET @add_pagos_year_check := IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'pagos'
       AND CONSTRAINT_TYPE = 'CHECK'
       AND CONSTRAINT_NAME = 'chk_pagos_anio') > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE pagos ADD CONSTRAINT chk_pagos_anio ',
        'CHECK (año_academico IS NULL OR año_academico BETWEEN 2000 AND 2100)')
);
PREPARE add_pagos_year_check_statement FROM @add_pagos_year_check;
EXECUTE add_pagos_year_check_statement;
DEALLOCATE PREPARE add_pagos_year_check_statement;

INSERT IGNORE INTO schema_migrations (version, description)
VALUES ('V006', 'Password rotation timestamp and data-range CHECK constraints');
