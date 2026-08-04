CREATE TYPE drop_status AS ENUM ('pending', 'approved', 'rejected');

CREATE TABLE cities (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    city_code VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_cities_country_code
        CHECK (country_code ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_cities_city_code
        CHECK (char_length(btrim(city_code)) > 0),
    CONSTRAINT uq_cities_country_code_city_code
        UNIQUE (country_code, city_code)
);

CREATE FUNCTION resolve_city(
    p_country_code VARCHAR(2),
    p_city_code VARCHAR(64)
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_city_id BIGINT;
BEGIN
    INSERT INTO cities (country_code, city_code)
    VALUES (upper(btrim(p_country_code)), btrim(p_city_code))
    ON CONFLICT (country_code, city_code) DO NOTHING
    RETURNING id INTO v_city_id;

    IF v_city_id IS NULL THEN
        SELECT id
        INTO v_city_id
        FROM cities
        WHERE country_code = upper(btrim(p_country_code))
          AND city_code = btrim(p_city_code);
    END IF;

    RETURN v_city_id;
END;
$$;

CREATE TABLE drops (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    audio_url TEXT NOT NULL,
    status drop_status NOT NULL DEFAULT 'pending',
    is_confession BOOLEAN NOT NULL DEFAULT FALSE,
    is_funny BOOLEAN NOT NULL DEFAULT FALSE,
    is_opinion BOOLEAN NOT NULL DEFAULT FALSE,
    is_student BOOLEAN NOT NULL DEFAULT FALSE,
    is_random BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '24 hours'),
    CONSTRAINT ck_drops_user_id
        CHECK (user_id > 0),
    CONSTRAINT ck_drops_audio_url
        CHECK (char_length(btrim(audio_url)) > 0),
    CONSTRAINT ck_drops_expiration
        CHECK (expires_at = created_at + INTERVAL '24 hours'),
    CONSTRAINT fk_drops_city
        FOREIGN KEY (city_id) REFERENCES cities (id)
);

CREATE INDEX idx_drops_city_status_id
    ON drops (city_id, status, id);

CREATE INDEX idx_drops_expires_at
    ON drops (expires_at);

COMMENT ON TABLE cities IS
    'Stable city identity from the client geocoder. country_code uses ISO 3166-1 alpha-2.';

COMMENT ON TABLE drops IS
    'Audio drops. user_id is a logical reference to the user stored in Neo4j.';

COMMENT ON COLUMN drops.audio_url IS
    'Store a stable object path or public URL, never a short-lived presigned upload URL.';
