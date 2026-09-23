ALTER TABLE cities
    ADD COLUMN latitude NUMERIC(9,6),
    ADD COLUMN longitude NUMERIC(9,6),
    ADD CONSTRAINT ck_cities_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT ck_cities_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);
