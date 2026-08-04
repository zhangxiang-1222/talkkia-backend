CREATE TABLE user_city_reactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    confession BOOLEAN NOT NULL DEFAULT FALSE,
    funny BOOLEAN NOT NULL DEFAULT FALSE,
    opinion BOOLEAN NOT NULL DEFAULT FALSE,
    student BOOLEAN NOT NULL DEFAULT FALSE,
    random BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_city_reactions_user_id
        CHECK (user_id > 0),
    CONSTRAINT uq_user_city_reactions_user_city
        UNIQUE (user_id, city_id),
    CONSTRAINT fk_user_city_reactions_city
        FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE CASCADE
);

COMMENT ON TABLE user_city_reactions IS
    'Per-user icon reaction state for each city. user_id is a logical reference to the user stored in Neo4j.';
