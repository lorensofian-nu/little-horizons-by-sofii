#!/usr/bin/env bash
# DESTRUCTIVE: full development reset of the academic database.
# Asks for an explicit confirmation word before dropping anything and uses
# absolute paths, so it never leaves the database dropped and empty because
# of a relative SOURCE path.
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
database="${ERP_DB_NAME:-erp_little_horizons}"
schema_file="${project_root}/docs/database.sql"

mysql_client="${MYSQL_CLIENT:-mysql}"

echo "This will DROP the database '${database}' and rebuild it from:"
echo "  ${schema_file}"
echo "All academic data in that database will be lost."
read -r -p "Type DESTROY to continue (anything else aborts): " confirmation
if [[ "${confirmation}" != "DESTROY" ]]; then
    echo "Aborted. The database was not modified."
    exit 1
fi

"${mysql_client}" --defaults-extra-file=<(printf '[client]\nuser=%s\npassword=%s\n' \
        "${ERP_DB_USER:?Set ERP_DB_USER}" "${ERP_DB_PASSWORD:?Set ERP_DB_PASSWORD}") \
    -e "DROP DATABASE IF EXISTS \`${database}\`;"

"${mysql_client}" --defaults-extra-file=<(printf '[client]\nuser=%s\npassword=%s\n' \
        "${ERP_DB_USER}" "${ERP_DB_PASSWORD}") \
    -e "CREATE DATABASE IF NOT EXISTS \`${database}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; USE \`${database}\`;" \
    < "${schema_file}"

echo "Database '${database}' rebuilt from docs/database.sql."
echo "Remember to recreate the initial administrator with ERP_INITIAL_ADMIN_PASSWORD."
