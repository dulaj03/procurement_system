-- ============================================================
--  V4__procurement_tables.sql
--  Supplier, Product, Inventory, Purchase Request/Order,
--  GRN, Invoice, Audit Log tables
-- ============================================================

-- ── Suppliers ────────────────────────────────────────────────
CREATE TABLE suppliers (
    id                  UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    code                VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(255),
    phone               VARCHAR(50),
    address             TEXT,
    city                VARCHAR(100),
    country             VARCHAR(100),
    tax_number          VARCHAR(100),
    registration_number VARCHAR(100),
    website             VARCHAR(255),
    payment_terms       VARCHAR(100),
    credit_limit        NUMERIC(18,4),
    rating              SMALLINT CHECK (rating BETWEEN 1 AND 5),
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    company_id          UUID         REFERENCES companies(id),
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    CONSTRAINT chk_supplier_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLACKLISTED'))
);

CREATE TABLE supplier_contacts (
    id          UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    supplier_id UUID        NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    designation VARCHAR(100),
    email       VARCHAR(255),
    phone       VARCHAR(50),
    is_primary  BOOLEAN     NOT NULL DEFAULT FALSE,
    is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

-- ── Product Categories ────────────────────────────────────────
CREATE TABLE product_categories (
    id          UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    description TEXT,
    parent_id   UUID         REFERENCES product_categories(id),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

-- ── Products ─────────────────────────────────────────────────
CREATE TABLE products (
    id              UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    sku             VARCHAR(100) NOT NULL UNIQUE,
    barcode         VARCHAR(100),
    description     TEXT,
    category_id     UUID         REFERENCES product_categories(id),
    unit_of_measure VARCHAR(50)  NOT NULL,
    unit_price      NUMERIC(18,4),
    reorder_level   INTEGER,
    reorder_quantity INTEGER,
    image_url       TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    company_id      UUID         REFERENCES companies(id),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    CONSTRAINT chk_product_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED'))
);

-- ── Inventory ────────────────────────────────────────────────
CREATE TABLE inventory (
    id                  UUID          DEFAULT uuid_generate_v4() PRIMARY KEY,
    product_id          UUID          NOT NULL REFERENCES products(id),
    branch_id           UUID          NOT NULL REFERENCES branches(id),
    quantity_on_hand    NUMERIC(18,4) NOT NULL DEFAULT 0,
    quantity_reserved   NUMERIC(18,4) NOT NULL DEFAULT 0,
    quantity_on_order   NUMERIC(18,4) NOT NULL DEFAULT 0,
    average_cost        NUMERIC(18,4),
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    CONSTRAINT uq_inventory_product_branch UNIQUE (product_id, branch_id)
);

-- ── Stock Movements ───────────────────────────────────────────
CREATE TABLE stock_movements (
    id               UUID          DEFAULT uuid_generate_v4() PRIMARY KEY,
    product_id       UUID          NOT NULL REFERENCES products(id),
    from_branch_id   UUID          REFERENCES branches(id),
    to_branch_id     UUID          REFERENCES branches(id),
    movement_type    VARCHAR(30)   NOT NULL,
    quantity         NUMERIC(18,4) NOT NULL,
    unit_cost        NUMERIC(18,4),
    reference_number VARCHAR(100),
    reference_type   VARCHAR(50),
    notes            TEXT,
    performed_by     UUID          REFERENCES users(id),
    is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255)
);

-- ── Purchase Requests ─────────────────────────────────────────
CREATE TABLE purchase_requests (
    id               UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    pr_number        VARCHAR(50)  NOT NULL UNIQUE,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    required_date    DATE,
    total_amount     NUMERIC(18,4),
    status           VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    priority         VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    rejection_reason TEXT,
    requested_by     UUID         NOT NULL REFERENCES users(id),
    approved_by      UUID         REFERENCES users(id),
    approved_at      TIMESTAMP,
    branch_id        UUID         NOT NULL REFERENCES branches(id),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255)
);

CREATE TABLE purchase_request_items (
    id                    UUID          DEFAULT uuid_generate_v4() PRIMARY KEY,
    purchase_request_id   UUID          NOT NULL REFERENCES purchase_requests(id) ON DELETE CASCADE,
    product_id            UUID          NOT NULL REFERENCES products(id),
    quantity              NUMERIC(18,4) NOT NULL,
    unit_of_measure       VARCHAR(50),
    estimated_unit_price  NUMERIC(18,4),
    estimated_total_price NUMERIC(18,4),
    specifications        TEXT,
    notes                 TEXT,
    is_deleted            BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255)
);

-- ── Purchase Orders ───────────────────────────────────────────
CREATE TABLE purchase_orders (
    id                    UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    po_number             VARCHAR(50)  NOT NULL UNIQUE,
    purchase_request_id   UUID         REFERENCES purchase_requests(id),
    supplier_id           UUID         NOT NULL REFERENCES suppliers(id),
    branch_id             UUID         NOT NULL REFERENCES branches(id),
    order_date            DATE         NOT NULL,
    expected_delivery_date DATE,
    delivery_address      TEXT,
    subtotal              NUMERIC(18,4),
    tax_amount            NUMERIC(18,4) DEFAULT 0,
    discount_amount       NUMERIC(18,4) DEFAULT 0,
    total_amount          NUMERIC(18,4),
    currency              VARCHAR(10)  NOT NULL DEFAULT 'USD',
    payment_terms         VARCHAR(100),
    notes                 TEXT,
    status                VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    created_by_user       UUID         REFERENCES users(id),
    approved_by           UUID         REFERENCES users(id),
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255)
);

CREATE TABLE purchase_order_items (
    id                 UUID          DEFAULT uuid_generate_v4() PRIMARY KEY,
    purchase_order_id  UUID          NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id         UUID          NOT NULL REFERENCES products(id),
    quantity_ordered   NUMERIC(18,4) NOT NULL,
    quantity_received  NUMERIC(18,4) NOT NULL DEFAULT 0,
    unit_price         NUMERIC(18,4) NOT NULL,
    discount_percent   NUMERIC(5,2)  DEFAULT 0,
    tax_percent        NUMERIC(5,2)  DEFAULT 0,
    total_price        NUMERIC(18,4),
    unit_of_measure    VARCHAR(50),
    notes              TEXT,
    is_deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP,
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255)
);

-- ── Goods Receipt Notes ───────────────────────────────────────
CREATE TABLE goods_receipt_notes (
    id                      UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    grn_number              VARCHAR(50)  NOT NULL UNIQUE,
    purchase_order_id       UUID         NOT NULL REFERENCES purchase_orders(id),
    branch_id               UUID         NOT NULL REFERENCES branches(id),
    receipt_date            DATE         NOT NULL,
    supplier_invoice_number VARCHAR(100),
    delivery_note_number    VARCHAR(100),
    notes                   TEXT,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    received_by             UUID         NOT NULL REFERENCES users(id),
    is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

CREATE TABLE grn_items (
    id                 UUID          DEFAULT uuid_generate_v4() PRIMARY KEY,
    grn_id             UUID          NOT NULL REFERENCES goods_receipt_notes(id) ON DELETE CASCADE,
    po_item_id         UUID          NOT NULL REFERENCES purchase_order_items(id),
    product_id         UUID          NOT NULL REFERENCES products(id),
    quantity_received  NUMERIC(18,4) NOT NULL,
    quantity_accepted  NUMERIC(18,4),
    quantity_rejected  NUMERIC(18,4) DEFAULT 0,
    unit_cost          NUMERIC(18,4),
    rejection_reason   TEXT,
    batch_number       VARCHAR(100),
    expiry_date        DATE,
    notes              TEXT,
    is_deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP,
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255)
);

-- ── Invoices ─────────────────────────────────────────────────
CREATE TABLE invoices (
    id                      UUID          DEFAULT uuid_generate_v4() PRIMARY KEY,
    invoice_number          VARCHAR(50)   NOT NULL UNIQUE,
    supplier_invoice_number VARCHAR(100),
    supplier_id             UUID          NOT NULL REFERENCES suppliers(id),
    purchase_order_id       UUID          REFERENCES purchase_orders(id),
    invoice_date            DATE          NOT NULL,
    due_date                DATE,
    subtotal                NUMERIC(18,4),
    tax_amount              NUMERIC(18,4) DEFAULT 0,
    total_amount            NUMERIC(18,4) NOT NULL,
    paid_amount             NUMERIC(18,4) DEFAULT 0,
    currency                VARCHAR(10)   DEFAULT 'USD',
    status                  VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    payment_date            DATE,
    notes                   TEXT,
    is_deleted              BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

-- ── Audit Logs ────────────────────────────────────────────────
CREATE TABLE audit_logs (
    id            UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     VARCHAR(100) NOT NULL,
    action        VARCHAR(50)  NOT NULL,
    performed_by  VARCHAR(255) NOT NULL,
    performed_at  TIMESTAMP    NOT NULL,
    old_values    TEXT,
    new_values    TEXT,
    ip_address    VARCHAR(50),
    user_agent    TEXT,
    notes         TEXT
);

-- ── Indexes ──────────────────────────────────────────────────
CREATE INDEX idx_suppliers_company         ON suppliers(company_id);
CREATE INDEX idx_products_category         ON products(category_id);
CREATE INDEX idx_products_company          ON products(company_id);
CREATE INDEX idx_inventory_product         ON inventory(product_id);
CREATE INDEX idx_inventory_branch          ON inventory(branch_id);
CREATE INDEX idx_stock_movements_product   ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_reference ON stock_movements(reference_number);
CREATE INDEX idx_pr_requested_by           ON purchase_requests(requested_by);
CREATE INDEX idx_pr_branch                 ON purchase_requests(branch_id);
CREATE INDEX idx_pr_status                 ON purchase_requests(status);
CREATE INDEX idx_po_supplier               ON purchase_orders(supplier_id);
CREATE INDEX idx_po_status                 ON purchase_orders(status);
CREATE INDEX idx_grn_po                    ON goods_receipt_notes(purchase_order_id);
CREATE INDEX idx_invoices_supplier         ON invoices(supplier_id);
CREATE INDEX idx_invoices_status           ON invoices(status);
CREATE INDEX idx_audit_entity              ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_performed_by        ON audit_logs(performed_by);
CREATE INDEX idx_audit_performed_at        ON audit_logs(performed_at);
