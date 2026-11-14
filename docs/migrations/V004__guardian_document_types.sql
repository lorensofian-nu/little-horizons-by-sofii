-- Adds PPT support for guardian documents while preserving CE values.

ALTER TABLE acudientes
    MODIFY COLUMN tipo_documento ENUM('CC', 'CE', 'Pasaporte', 'PPT')
    NOT NULL DEFAULT 'CC';

INSERT IGNORE INTO schema_migrations (version, description)
VALUES ('V004', 'Guardian document type expansion');
