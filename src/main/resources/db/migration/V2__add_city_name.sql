ALTER TABLE cities
    ADD COLUMN city_name VARCHAR(128);

ALTER TABLE cities
    ADD CONSTRAINT ck_cities_city_name
    CHECK (city_name IS NULL OR char_length(btrim(city_name)) > 0);

DROP FUNCTION resolve_city(VARCHAR, VARCHAR);

CREATE FUNCTION resolve_city(
    p_country_code VARCHAR(2),
    p_city_code VARCHAR(64),
    p_city_name VARCHAR(128)
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_city_id BIGINT;
BEGIN
    INSERT INTO cities (country_code, city_code, city_name)
    VALUES (
        upper(btrim(p_country_code)),
        btrim(p_city_code),
        btrim(p_city_name)
    )
    ON CONFLICT (country_code, city_code) DO UPDATE
    SET city_name = COALESCE(cities.city_name, EXCLUDED.city_name)
    RETURNING id INTO v_city_id;

    RETURN v_city_id;
END;
$$;
