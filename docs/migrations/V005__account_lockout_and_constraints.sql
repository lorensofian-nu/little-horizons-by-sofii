-- Upgrade: account lockout support plus integrity constraint refinements.
-- MySQL 8.0 compatible and idempotent: every schema change is guarded through
-- information_schema + PREPARE, so the file is safe to re-run.
-- Execute once, after selecting erp_little_horizons.

-- ----------------------------------------------------------------------------
-- 1. Account lockout columns (usuarios)
-- ----------------------------------------------------------------------------
SET @has_failed_attempts := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usuarios'
      AND COLUMN_NAME = 'intentos_fallidos'
);
SET @add_lockout_columns := IF(
    @has_failed_attempts > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE usuarios ',
        'ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0, ',
        'ADD COLUMN bloqueado_hasta TIMESTAMP NULL, ',
        'ADD INDEX idx_usuario_bloqueo (nombre_usuario, bloqueado_hasta)')
);
PREPARE add_lockout_columns_statement FROM @add_lockout_columns;
EXECUTE add_lockout_columns_statement;
DEALLOCATE PREPARE add_lockout_columns_statement;

-- ----------------------------------------------------------------------------
-- 2. Unique e-mail for estudiantes and acudientes.
--    Duplicate e-mails are anonymized first (only the lowest id keeps them)
--    so the unique index cannot fail on legacy data.
-- ----------------------------------------------------------------------------
UPDATE estudiantes e
JOIN estudiantes d
  ON d.correo_electronico = e.correo_electronico
 AND d.id_estudiante < e.id_estudiante
SET e.correo_electronico = NULL
WHERE e.correo_electronico IS NOT NULL;

UPDATE acudientes a
JOIN acudientes d
  ON d.correo_electronico = a.correo_electronico
 AND d.id_acudiente < a.id_acudiente
SET a.correo_electronico = NULL
WHERE a.correo_electronico IS NOT NULL;

SET @has_student_email_unique := (
    SELECT COUNT(DISTINCT INDEX_NAME) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'estudiantes'
      AND INDEX_NAME = 'uq_estudiante_correo'
);
SET @add_student_email_unique := IF(
    @has_student_email_unique > 0,
    'SELECT 1',
    'ALTER TABLE estudiantes ADD UNIQUE KEY uq_estudiante_correo (correo_electronico)'
);
PREPARE add_student_email_unique_statement FROM @add_student_email_unique;
EXECUTE add_student_email_unique_statement;
DEALLOCATE PREPARE add_student_email_unique_statement;

SET @has_guardian_email_unique := (
    SELECT COUNT(DISTINCT INDEX_NAME) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'acudientes'
      AND INDEX_NAME = 'uq_acudiente_correo'
);
SET @add_guardian_email_unique := IF(
    @has_guardian_email_unique > 0,
    'SELECT 1',
    'ALTER TABLE acudientes ADD UNIQUE KEY uq_acudiente_correo (correo_electronico)'
);
PREPARE add_guardian_email_unique_statement FROM @add_guardian_email_unique;
EXECUTE add_guardian_email_unique_statement;
DEALLOCATE PREPARE add_guardian_email_unique_statement;

-- ----------------------------------------------------------------------------
-- 3. Nullable teacher references now release on delete (ON DELETE SET NULL):
--    club supervisor, event organizer and attendance recorder.
-- ----------------------------------------------------------------------------
SET @club_supervisor_fk := (
    SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clubes'
      AND COLUMN_NAME = 'id_profesor_supervisor'
      AND REFERENCED_TABLE_NAME = 'profesores'
    LIMIT 1
);
SET @drop_club_supervisor_fk := IF(
    @club_supervisor_fk IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE clubes DROP FOREIGN KEY ', @club_supervisor_fk)
);
PREPARE drop_club_supervisor_fk_statement FROM @drop_club_supervisor_fk;
EXECUTE drop_club_supervisor_fk_statement;
DEALLOCATE PREPARE drop_club_supervisor_fk_statement;
SET @add_club_supervisor_fk := IF(
    @club_supervisor_fk IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE clubes ADD CONSTRAINT fk_clubes_profesor_supervisor ',
        'FOREIGN KEY (id_profesor_supervisor) REFERENCES profesores(id_profesor) ',
        'ON DELETE SET NULL')
);
PREPARE add_club_supervisor_fk_statement FROM @add_club_supervisor_fk;
EXECUTE add_club_supervisor_fk_statement;
DEALLOCATE PREPARE add_club_supervisor_fk_statement;

SET @event_organizer_fk := (
    SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'eventos'
      AND COLUMN_NAME = 'id_organizador'
      AND REFERENCED_TABLE_NAME = 'profesores'
    LIMIT 1
);
SET @drop_event_organizer_fk := IF(
    @event_organizer_fk IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE eventos DROP FOREIGN KEY ', @event_organizer_fk)
);
PREPARE drop_event_organizer_fk_statement FROM @drop_event_organizer_fk;
EXECUTE drop_event_organizer_fk_statement;
DEALLOCATE PREPARE drop_event_organizer_fk_statement;
SET @add_event_organizer_fk := IF(
    @event_organizer_fk IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE eventos ADD CONSTRAINT fk_eventos_organizador ',
        'FOREIGN KEY (id_organizador) REFERENCES profesores(id_profesor) ',
        'ON DELETE SET NULL')
);
PREPARE add_event_organizer_fk_statement FROM @add_event_organizer_fk;
EXECUTE add_event_organizer_fk_statement;
DEALLOCATE PREPARE add_event_organizer_fk_statement;

SET @attendance_recorder_fk := (
    SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'asistencias'
      AND COLUMN_NAME = 'registrado_por'
      AND REFERENCED_TABLE_NAME = 'profesores'
    LIMIT 1
);
SET @drop_attendance_recorder_fk := IF(
    @attendance_recorder_fk IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE asistencias DROP FOREIGN KEY ', @attendance_recorder_fk)
);
PREPARE drop_attendance_recorder_fk_statement FROM @drop_attendance_recorder_fk;
EXECUTE drop_attendance_recorder_fk_statement;
DEALLOCATE PREPARE drop_attendance_recorder_fk_statement;
SET @add_attendance_recorder_fk := IF(
    @attendance_recorder_fk IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE asistencias ADD CONSTRAINT fk_asistencias_registrado_por ',
        'FOREIGN KEY (registrado_por) REFERENCES profesores(id_profesor) ',
        'ON DELETE SET NULL')
);
PREPARE add_attendance_recorder_fk_statement FROM @add_attendance_recorder_fk;
EXECUTE add_attendance_recorder_fk_statement;
DEALLOCATE PREPARE add_attendance_recorder_fk_statement;

INSERT IGNORE INTO schema_migrations (version, description)
VALUES ('V005', 'Account lockout, unique e-mails and nullable teacher references');
