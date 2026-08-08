-- ============================================================
--  V3__seed_super_admin.sql
--  Creates the initial super admin user
--  Password: Admin@12345 (bcrypt hash)
-- ============================================================

-- ── Default Company ──────────────────────────────────────────
INSERT INTO companies (id, name, code, status) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Procurement Corp', 'PROC001', 'ACTIVE');

-- ── Default Branch ───────────────────────────────────────────
INSERT INTO branches (id, name, code, company_id, status) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Head Office', 'HQ', 'c0000000-0000-0000-0000-000000000001', 'ACTIVE');

-- ── Super Admin User ─────────────────────────────────────────
-- Password: Admin@12345
INSERT INTO users (id, first_name, last_name, email, password_hash, employee_code, status, company_id, branch_id) VALUES
    ('u0000000-0000-0000-0000-000000000001',
     'Super',
     'Admin',
     'admin@procure.com',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCbV3s0CshAH0VuGJjcC3qK',
     'EMP-001',
     'ACTIVE',
     'c0000000-0000-0000-0000-000000000001',
     'b0000000-0000-0000-0000-000000000001');

-- ── Assign SUPER_ADMIN role ───────────────────────────────────
INSERT INTO user_roles (user_id, role_id) VALUES
    ('u0000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001');
