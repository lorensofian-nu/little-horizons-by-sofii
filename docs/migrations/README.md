# Versioned migrations

`docs/database.sql` is the non-destructive baseline for a new academic
installation. Existing installations must be upgraded with the numbered SQL
files in this directory, in ascending order, after selecting the target
database.

The `schema_migrations` table records applied versions. `V002` upgrades legacy
invoice storage from the old PDF-named column to `nombre_archivo_txt`, creates
the document sequence table and creates the authenticated-users table without
inserting a known password. `V003` requires and protects one primary guardian
for every student, including synchronization triggers for direct database
updates. `V004` adds `PPT` as a guardian document type while preserving the
existing `CE` database value for Cédula de Extranjería.

Example:

```sql
USE erp_little_horizons;
SOURCE docs/migrations/V001__baseline.sql;
SOURCE docs/migrations/V002__security_and_txt.sql;
SOURCE docs/migrations/V003__student_primary_guardian_integrity.sql;
SOURCE docs/migrations/V004__guardian_document_types.sql;
```

Do not put passwords or personal data in migration files.
