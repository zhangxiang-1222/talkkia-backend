CREATE TABLE user_city_drop_progress (
    user_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    last_listened_drop_id BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_city_drop_progress
        PRIMARY KEY (user_id, city_id),
    CONSTRAINT ck_user_city_drop_progress_user_id
        CHECK (user_id > 0),
    CONSTRAINT ck_user_city_drop_progress_last_listened_drop_id
        CHECK (last_listened_drop_id >= 0),
    CONSTRAINT fk_user_city_drop_progress_city
        FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE CASCADE
);

COMMENT ON TABLE user_city_drop_progress IS
    'Per-user playback cursor for each city. user_id is a logical reference to the user stored in Neo4j.';

COMMENT ON COLUMN user_city_drop_progress.last_listened_drop_id IS
    'Highest completed drop ID. This is intentionally not a foreign key because expired drops may be removed.';
