-- ============================================================
--  V2__seed_roles_permissions.sql
--  Seed default roles and permissions for RBAC
-- ============================================================

-- ── Permissions ──────────────────────────────────────────────
INSERT INTO permissions (id, name, description, module) VALUES
    -- User Management
    (uuid_generate_v4(), 'USER:READ',            'View users',           'USER'),
    (uuid_generate_v4(), 'USER:WRITE',           'Create/update users',  'USER'),
    (uuid_generate_v4(), 'USER:DELETE',          'Delete users',         'USER'),

    -- Company & Branch
    (uuid_generate_v4(), 'COMPANY:READ',         'View companies',       'COMPANY'),
    (uuid_generate_v4(), 'COMPANY:WRITE',        'Manage companies',     'COMPANY'),

    -- Supplier
    (uuid_generate_v4(), 'SUPPLIER:READ',        'View suppliers',       'SUPPLIER'),
    (uuid_generate_v4(), 'SUPPLIER:WRITE',       'Manage suppliers',     'SUPPLIER'),

    -- Product & Inventory
    (uuid_generate_v4(), 'PRODUCT:READ',         'View products',        'PRODUCT'),
    (uuid_generate_v4(), 'PRODUCT:WRITE',        'Manage products',      'PRODUCT'),
    (uuid_generate_v4(), 'INVENTORY:READ',       'View inventory',       'INVENTORY'),
    (uuid_generate_v4(), 'INVENTORY:WRITE',      'Manage inventory',     'INVENTORY'),

    -- Purchase Requests
    (uuid_generate_v4(), 'PR:CREATE',            'Create purchase requests',  'PURCHASE'),
    (uuid_generate_v4(), 'PR:READ',              'View purchase requests',    'PURCHASE'),
    (uuid_generate_v4(), 'PR:APPROVE',           'Approve purchase requests', 'PURCHASE'),
    (uuid_generate_v4(), 'PR:REJECT',            'Reject purchase requests',  'PURCHASE'),

    -- Purchase Orders
    (uuid_generate_v4(), 'PO:READ',              'View purchase orders',  'PURCHASE'),
    (uuid_generate_v4(), 'PO:WRITE',             'Create purchase orders','PURCHASE'),
    (uuid_generate_v4(), 'PO:APPROVE',           'Approve purchase orders','PURCHASE'),

    -- Receiving
    (uuid_generate_v4(), 'RECEIVING:READ',       'View GRNs',            'RECEIVING'),
    (uuid_generate_v4(), 'RECEIVING:WRITE',      'Create GRNs',          'RECEIVING'),

    -- Invoice
    (uuid_generate_v4(), 'INVOICE:READ',         'View invoices',        'INVOICE'),
    (uuid_generate_v4(), 'INVOICE:WRITE',        'Manage invoices',      'INVOICE'),

    -- Reports & Dashboard
    (uuid_generate_v4(), 'DASHBOARD:READ',       'View dashboard',       'DASHBOARD'),
    (uuid_generate_v4(), 'REPORT:GENERATE',      'Generate reports',     'REPORT'),

    -- Admin
    (uuid_generate_v4(), 'ADMIN:FULL',           'Full system access',   'ADMIN');

-- ── Roles ────────────────────────────────────────────────────
INSERT INTO roles (id, name, description) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'ROLE_SUPER_ADMIN',    'Full platform access'),
    ('a1000000-0000-0000-0000-000000000002', 'ROLE_ADMIN',          'Company administrator'),
    ('a1000000-0000-0000-0000-000000000003', 'ROLE_MANAGER',        'Department manager with approval rights'),
    ('a1000000-0000-0000-0000-000000000004', 'ROLE_PROCUREMENT',    'Procurement officer'),
    ('a1000000-0000-0000-0000-000000000005', 'ROLE_WAREHOUSE',      'Warehouse staff'),
    ('a1000000-0000-0000-0000-000000000006', 'ROLE_FINANCE',        'Finance/accounts team'),
    ('a1000000-0000-0000-0000-000000000007', 'ROLE_EMPLOYEE',       'Regular employee — read-only on most modules');

-- ── Super Admin gets ALL permissions ─────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a1000000-0000-0000-0000-000000000001', id FROM permissions;

-- ── Admin gets most permissions (no ADMIN:FULL) ───────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a1000000-0000-0000-0000-000000000002', id
FROM permissions WHERE name != 'ADMIN:FULL';

-- ── Manager permissions ───────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a1000000-0000-0000-0000-000000000003', id
FROM permissions WHERE name IN (
    'USER:READ', 'SUPPLIER:READ', 'PRODUCT:READ', 'INVENTORY:READ',
    'PR:CREATE', 'PR:READ', 'PR:APPROVE', 'PR:REJECT',
    'PO:READ', 'PO:APPROVE', 'RECEIVING:READ', 'INVOICE:READ',
    'DASHBOARD:READ', 'REPORT:GENERATE'
);

-- ── Procurement officer ───────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a1000000-0000-0000-0000-000000000004', id
FROM permissions WHERE name IN (
    'SUPPLIER:READ', 'SUPPLIER:WRITE', 'PRODUCT:READ', 'INVENTORY:READ',
    'PR:CREATE', 'PR:READ', 'PO:READ', 'PO:WRITE',
    'RECEIVING:READ', 'INVOICE:READ', 'DASHBOARD:READ'
);

-- ── Warehouse staff ───────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a1000000-0000-0000-0000-000000000005', id
FROM permissions WHERE name IN (
    'PRODUCT:READ', 'INVENTORY:READ', 'INVENTORY:WRITE',
    'RECEIVING:READ', 'RECEIVING:WRITE', 'PO:READ', 'DASHBOARD:READ'
);

-- ── Finance ───────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a1000000-0000-0000-0000-000000000006', id
FROM permissions WHERE name IN (
    'PO:READ', 'INVOICE:READ', 'INVOICE:WRITE',
    'DASHBOARD:READ', 'REPORT:GENERATE'
);

-- ── Regular employee (read-only basics) ──────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a1000000-0000-0000-0000-000000000007', id
FROM permissions WHERE name IN (
    'PR:CREATE', 'PR:READ', 'PRODUCT:READ', 'DASHBOARD:READ'
);
