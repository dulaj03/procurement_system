-- ============================================================
--  V1__init_schema.sql
--  Initial database schema for procurement system
--  Managed by Flyway
-- ============================================================

-- ── Extensions ───────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Companies ────────────────────────────────────────────────
CREATE TABLE companies (
    id                  UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    code                VARCHAR(50)  NOT NULL UNIQUE,
    registration_number VARCHAR(100) UNIQUE,
    tax_number          VARCHAR(100),
    address             TEXT,
    city                VARCHAR(100),
    country             VARCHAR(100),
    email               VARCHAR(255),
    phone               VARCHAR(50),
    logo_url            TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    CONSTRAINT chk_company_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- ── Branches ─────────────────────────────────────────────────
CREATE TABLE branches (
    id          UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    address     TEXT,
    city        VARCHAR(100),
    country     VARCHAR(100),
    phone       VARCHAR(50),
    email       VARCHAR(255),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    company_id  UUID         NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT chk_branch_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT uq_branch_code_per_company UNIQUE (company_id, code)
);

-- ── Permissions ──────────────────────────────────────────────
CREATE TABLE permissions (
    id          UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    module      VARCHAR(100),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

-- ── Roles ────────────────────────────────────────────────────
CREATE TABLE roles (
    id          UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

-- ── Role <-> Permission mapping ───────────────────────────────
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    id              UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    employee_code   VARCHAR(50)  UNIQUE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    company_id      UUID         REFERENCES companies(id),
    branch_id       UUID         REFERENCES branches(id),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

-- ── User <-> Role mapping ─────────────────────────────────────
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ── Indexes ──────────────────────────────────────────────────
CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_company     ON users(company_id);
CREATE INDEX idx_users_branch      ON users(branch_id);
CREATE INDEX idx_branches_company  ON branches(company_id);
