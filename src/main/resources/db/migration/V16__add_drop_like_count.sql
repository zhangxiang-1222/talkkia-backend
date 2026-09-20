ALTER TABLE drops
    ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_drops_like_count_non_negative
        CHECK (like_count >= 0);
