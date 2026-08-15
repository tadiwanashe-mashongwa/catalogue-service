CREATE TABLE brands (
    id UUID PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    parent_id UUID
);

CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregate_id UUID,
    aggregate_type VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE,
    event_type VARCHAR(255),
    payload VARCHAR(255)
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255),
    aggregate_type VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE,
    event_type VARCHAR(255),
    payload TEXT,
    status VARCHAR(255),
    CONSTRAINT outbox_events_status_check
        CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED'))
);

CREATE TABLE parts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount BIGINT,
    currency VARCHAR(255),
    sku VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    brand_id UUID NOT NULL REFERENCES brands(id),
    category_id UUID NOT NULL REFERENCES categories(id),
    CONSTRAINT parts_currency_check CHECK (currency IN ('USD', 'ZWL')),
    CONSTRAINT parts_status_check CHECK (status IN ('ACTIVE', 'DISCONTINUED'))
);

CREATE TABLE part_images (
    id UUID PRIMARY KEY,
    embedding_id UUID,
    url VARCHAR(255),
    part_id UUID REFERENCES parts(id)
);

CREATE TABLE vehicle_fitments (
    id UUID PRIMARY KEY,
    make VARCHAR(255),
    model VARCHAR(255),
    year_from INTEGER NOT NULL,
    year_to INTEGER NOT NULL,
    part_id UUID REFERENCES parts(id)
);
