CREATE TABLE store_hours
(
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id               BIGINT                                                        NOT NULL REFERENCES store (id) ON DELETE CASCADE,
    day_of_week            VARCHAR CHECK ( day_of_week IN
                                           ('MONDAY', 'TUESDAY', 'WEDNESDAY',
                                            'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')) NOT NULL,
    open_time              TIME,
    close_time             TIME,
    is_day_off             boolean                                                       NOT NULL DEFAULT FALSE,
    last_order_cutoff_time TIME,
    created_at             TIMESTAMPTZ                                                   NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ                                                   NOT NULL DEFAULT NOW(),
    UNIQUE (day_of_week, store_id)
);

CREATE TABLE store_breaks
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_hours_id BIGINT      NOT NULL REFERENCES store_hours (id) ON DELETE CASCADE,
    start_time     TIME        NOT NULL,
    end_time       TIME        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (start_time < end_time)
);