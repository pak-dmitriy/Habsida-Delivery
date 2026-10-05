CREATE TABLE order_payments
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id       BIGINT                                                            NOT NULL REFERENCES orders (id),
    transaction_id BIGINT,
    method         VARCHAR(50) CHECK (method IN ('ONLINE', 'CASH', 'BANK_TRANSFER')) NOT NULL,
    status         VARCHAR(50) CHECK (status IN ('PAID', 'UNPAID', 'REFUNDED'))      NOT NULL,
    paid_at        TIMESTAMPTZ,
    provider       VARCHAR(50),
    created_at     TIMESTAMPTZ                                                       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ                                                       NOT NULL DEFAULT NOW()
);

CREATE TABLE transactions
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_payment_id BIGINT                                                         NOT NULL REFERENCES order_payments (id),
    status           VARCHAR(50) CHECK (status IN ('SUCCESS', 'PENDING', 'FAILED')) NOT NULL,
    amount           NUMERIC(10, 2)                                                 NOT NULL,
    created_at       TIMESTAMPTZ                                                    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ                                                    NOT NULL DEFAULT NOW()
);

ALTER TABLE order_payments
    ADD CONSTRAINT fk_order_payments_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id);

