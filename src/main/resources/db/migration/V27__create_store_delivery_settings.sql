CREATE TABLE store_delivery_settings
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id                BIGINT         NOT NULL UNIQUE REFERENCES store (id),
    delivery_enabled        BOOLEAN        NOT NULL DEFAULT FALSE,
    delivery_type           VARCHAR(20)    NOT NULL CHECK (delivery_type IN ('SAME_DAY', 'SCHEDULED')),
    min_order_price         NUMERIC(10, 2) NOT NULL,
    free_delivery_threshold NUMERIC(10, 2) NOT NULL,
    max_distance            NUMERIC(10, 2) NOT NULL,
    delivery_geography      VARCHAR(100)   NOT NULL CHECK (delivery_geography IN ('ONLY_IN_THE_CITY', 'CITY_AND_SUBURBS')),
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE store_delivery_areas
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id      BIGINT         NOT NULL REFERENCES store (id),
    district_name VARCHAR(100)   NOT NULL,
    delivery_fee  NUMERIC(10, 2) NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, district_name)
);

CREATE TABLE store_delivery_restrictions
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id        BIGINT       NOT NULL REFERENCES store (id),
    restricted_area VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, restricted_area)
);