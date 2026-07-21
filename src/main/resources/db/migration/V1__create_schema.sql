CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price > 0),
    current_stock INTEGER NOT NULL CHECK (current_stock >= 0),
    reorder_level INTEGER NOT NULL CHECK (reorder_level >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_products_active_name ON products (active, name);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('INBOUND', 'OUTBOUND')),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    resulting_stock INTEGER NOT NULL CHECK (resulting_stock >= 0),
    reason VARCHAR(200) NOT NULL,
    reference VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_movements_product_created ON stock_movements (product_id, created_at DESC);
