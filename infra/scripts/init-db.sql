-- init-db.sql
-- Runs once when Postgres container is first created
-- Extensions are also applied by Flyway V1, this is a safety net

SELECT 'Creating procure_db extensions...' AS status;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
