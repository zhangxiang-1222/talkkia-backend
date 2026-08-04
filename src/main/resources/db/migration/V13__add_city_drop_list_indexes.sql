CREATE INDEX idx_cities_city_code
    ON cities (city_code);

CREATE INDEX idx_drops_approved_city_created_at
    ON drops (city_id, created_at DESC, id DESC)
    WHERE status = 'approved'::drop_status;
