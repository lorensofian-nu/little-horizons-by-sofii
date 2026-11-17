-- Requires V001/V002 and an existing erp_little_horizons schema.
-- Enforces one primary guardian per student and keeps both sides synchronized.
-- MySQL 8.0 compatible: ADD COLUMN IF NOT EXISTS is MariaDB-only syntax, so
-- every schema change is guarded through information_schema + PREPARE and is
-- safe to re-run.

SET @has_primary_guardian_column := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'estudiantes'
      AND COLUMN_NAME = 'id_acudiente_principal'
);
SET @add_primary_guardian_column := IF(
    @has_primary_guardian_column > 0,
    'SELECT 1',
    'ALTER TABLE estudiantes ADD COLUMN id_acudiente_principal INT NULL'
);
PREPARE add_primary_guardian_column_statement FROM @add_primary_guardian_column;
EXECUTE add_primary_guardian_column_statement;
DEALLOCATE PREPARE add_primary_guardian_column_statement;

-- Backfill existing students from their current primary link.
UPDATE estudiantes e
JOIN estudiante_acudiente ea
  ON ea.id_estudiante = e.id_estudiante
 AND ea.es_principal = TRUE
SET e.id_acudiente_principal = ea.id_acudiente
WHERE e.id_acudiente_principal IS NULL;

-- Make the column mandatory and referenced. Only executed when the foreign
-- key is absent, so the statement (and the NOT NULL enforcement) is not
-- repeated on already-migrated installations.
SET @has_primary_guardian_fk := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'estudiantes'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
      AND CONSTRAINT_NAME = 'fk_estudiante_acudiente_principal'
);
SET @enforce_primary_guardian_fk := IF(
    @has_primary_guardian_fk > 0,
    'SELECT 1',
    'ALTER TABLE estudiantes '
        || 'MODIFY COLUMN id_acudiente_principal INT NOT NULL, '
        || 'ADD CONSTRAINT fk_estudiante_acudiente_principal '
        || 'FOREIGN KEY (id_acudiente_principal) '
        || 'REFERENCES acudientes(id_acudiente) '
        || 'ON DELETE RESTRICT'
);
PREPARE enforce_primary_guardian_fk_statement FROM @enforce_primary_guardian_fk;
EXECUTE enforce_primary_guardian_fk_statement;
DEALLOCATE PREPARE enforce_primary_guardian_fk_statement;

SET @has_unique_link_column := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'estudiante_acudiente'
      AND COLUMN_NAME = 'id_estudiante_principal_unico'
);
SET @add_unique_link_column := IF(
    @has_unique_link_column > 0,
    'SELECT 1',
    'ALTER TABLE estudiante_acudiente '
        || 'ADD COLUMN id_estudiante_principal_unico INT GENERATED ALWAYS AS '
        || '(CASE WHEN es_principal THEN id_estudiante ELSE NULL END) STORED'
);
PREPARE add_unique_link_column_statement FROM @add_unique_link_column;
EXECUTE add_unique_link_column_statement;
DEALLOCATE PREPARE add_unique_link_column_statement;

SET @has_unique_link_key := (
    SELECT COUNT(DISTINCT INDEX_NAME) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'estudiante_acudiente'
      AND INDEX_NAME = 'uq_estudiante_guardian_principal'
);
SET @add_unique_link_key := IF(
    @has_unique_link_key > 0,
    'SELECT 1',
    'ALTER TABLE estudiante_acudiente '
        || 'ADD UNIQUE KEY uq_estudiante_guardian_principal '
        || '(id_estudiante_principal_unico)'
);
PREPARE add_unique_link_key_statement FROM @add_unique_link_key;
EXECUTE add_unique_link_key_statement;
DEALLOCATE PREPARE add_unique_link_key_statement;

DROP TRIGGER IF EXISTS trg_estudiantes_after_insert_guardian;
DROP TRIGGER IF EXISTS trg_estudiantes_after_update_guardian;
DROP TRIGGER IF EXISTS trg_student_guardian_after_insert_primary;
DROP TRIGGER IF EXISTS trg_student_guardian_before_update_primary;
DROP TRIGGER IF EXISTS trg_student_guardian_before_delete_primary;

DELIMITER $$
CREATE TRIGGER trg_estudiantes_after_insert_guardian
AFTER INSERT ON estudiantes
FOR EACH ROW
BEGIN
    INSERT INTO estudiante_acudiente
        (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia)
    VALUES (NEW.id_estudiante, NEW.id_acudiente_principal, TRUE, TRUE);
END$$

CREATE TRIGGER trg_estudiantes_after_update_guardian
AFTER UPDATE ON estudiantes
FOR EACH ROW
BEGIN
    IF NOT (NEW.id_acudiente_principal <=> OLD.id_acudiente_principal) THEN
        UPDATE estudiante_acudiente
        SET es_principal = FALSE
        WHERE id_estudiante = NEW.id_estudiante
          AND es_principal = TRUE
          AND id_acudiente <> NEW.id_acudiente_principal;

        INSERT INTO estudiante_acudiente
            (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia)
        VALUES (NEW.id_estudiante, NEW.id_acudiente_principal, TRUE, TRUE)
        ON DUPLICATE KEY UPDATE
            es_principal = TRUE,
            es_contacto_emergencia = TRUE;
    END IF;
END$$

CREATE TRIGGER trg_student_guardian_after_insert_primary
AFTER INSERT ON estudiante_acudiente
FOR EACH ROW
BEGIN
    IF NEW.es_principal = TRUE THEN
        UPDATE estudiantes
        SET id_acudiente_principal = NEW.id_acudiente
        WHERE id_estudiante = NEW.id_estudiante
          AND id_acudiente_principal <> NEW.id_acudiente;
    END IF;
END$$

CREATE TRIGGER trg_student_guardian_before_update_primary
BEFORE UPDATE ON estudiante_acudiente
FOR EACH ROW
BEGIN
    IF OLD.es_principal = TRUE AND NEW.es_principal = FALSE
       AND OLD.id_acudiente = (
           SELECT id_acudiente_principal
           FROM estudiantes
           WHERE id_estudiante = OLD.id_estudiante
       ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Assign another primary guardian before demoting this one';
    END IF;
END$$

CREATE TRIGGER trg_student_guardian_before_delete_primary
BEFORE DELETE ON estudiante_acudiente
FOR EACH ROW
BEGIN
    IF OLD.id_acudiente = (
        SELECT id_acudiente_principal
        FROM estudiantes
        WHERE id_estudiante = OLD.id_estudiante
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A student must keep a primary guardian';
    END IF;
END$$
DELIMITER ;

INSERT IGNORE INTO schema_migrations (version, description)
VALUES ('V003', 'Primary guardian and student relationship integrity');
