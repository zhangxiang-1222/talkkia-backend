DROP TABLE user_city_reactions;

CREATE TABLE user_drop_reactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    drop_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    liked BOOLEAN NOT NULL DEFAULT FALSE,
    laughed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_drop_reactions_drop
        FOREIGN KEY (drop_id) REFERENCES drops (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_drop_reactions_drop_user
        UNIQUE (drop_id, user_id),
    CONSTRAINT ck_user_drop_reactions_user_id
        CHECK (user_id > 0),
    CONSTRAINT ck_user_drop_reactions_selected
        CHECK (liked OR laughed)
);

COMMENT ON TABLE user_drop_reactions IS
    'Per-user like and laugh state for each drop. user_id is a logical reference to the user stored in Neo4j.';
