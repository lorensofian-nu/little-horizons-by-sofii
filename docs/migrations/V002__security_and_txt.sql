-- Upgrade for installations created before the security/TXT hardening.
-- Execute once, after selecting erp_little_horizons.
CREATE TABLE IF NOT EXISTS schema_migrations (
    version VARCHAR(32) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS secuencias_documento (
    nombre_secuencia VARCHAR(80) PRIMARY KEY,
    siguiente_valor INT NOT NULL,
    CHECK (siguiente_valor > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(80) NOT NULL UNIQUE,
    hash_password VARCHAR(255) NOT NULL,
    rol ENUM('Administrador', 'Profesor', 'Secretaria') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    debe_cambiar_password BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso TIMESTAMP NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

SET @has_legacy_invoice_column := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'facturas'
      AND COLUMN_NAME = 'nombre_archivo_pdf'
);
SET @rename_invoice_column := IF(
    @has_legacy_invoice_column > 0,
    'ALTER TABLE facturas CHANGE COLUMN nombre_archivo_pdf nombre_archivo_txt VARCHAR(255)',
    'SELECT 1'
);
PREPARE rename_invoice_column_statement FROM @rename_invoice_column;
EXECUTE rename_invoice_column_statement;
DEALLOCATE PREPARE rename_invoice_column_statement;

INSERT IGNORE INTO schema_migrations (version, description)
VALUES ('V002', 'Security tables, document sequences and TXT invoice column');
