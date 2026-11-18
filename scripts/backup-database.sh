#!/usr/bin/env bash
# Creates a compressed backup of the ERP database.
#
# Credentials are read from the ERP_DB_* environment variables when set, or
# from the untracked db.properties file at the project root. The dump is
# written to backups/ with a timestamp and excluded from version control.
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
database="${ERP_DB_NAME:-erp_little_horizons}"
backup_dir="${project_root}/backups"

# --- credentials -------------------------------------------------------------
user="${ERP_DB_USER:-}"
password="${ERP_DB_PASSWORD:-}"
if [[ -z "${user}" || -z "${password}" ]]; then
    properties="${project_root}/db.properties"
    if [[ -f "${properties}" ]]; then
        user="$(sed -n 's/^db\.user=//p' "${properties}")"
        password="$(sed -n 's/^db\.password=//p' "${properties}")"
        host="$(sed -n 's/^db\.host=//p' "${properties}")"
        port="$(sed -n 's/^db\.port=//p' "${properties}")"
    fi
fi
host="${ERP_DB_HOST:-${host:-localhost}}"
port="${ERP_DB_PORT:-${port:-3306}}"
if [[ -z "${user}" || -z "${password}" ]]; then
    echo "Error: set ERP_DB_USER and ERP_DB_PASSWORD or provide db.properties." >&2
    exit 1
fi

# --- dump --------------------------------------------------------------------
mkdir -p "${backup_dir}"
chmod 700 "${backup_dir}" 2>/dev/null || true
timestamp="$(date '+%Y%m%d_%H%M%S')"
target="${backup_dir}/${database}_${timestamp}.sql.gz"

mysqldump --host="${host}" --port="${port}" --user="${user}" --password="${password}" \
    --single-transaction --routines --triggers --events \
    --default-character-set=utf8mb4 \
    "${database}" | gzip > "${target}"

chmod 600 "${target}" 2>/dev/null || true
echo "Backup written to ${target}"
