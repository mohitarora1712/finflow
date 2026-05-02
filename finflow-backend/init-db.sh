#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  CREATE DATABASE "auth-service_db";
  CREATE DATABASE application_service_db;
  CREATE DATABASE document_service_db;
  CREATE DATABASE admin_service_db;
EOSQL