ALTER TABLE cities
    DROP CONSTRAINT ck_cities_engagement_counters_non_negative,
    DROP COLUMN confession,
    DROP COLUMN funny,
    DROP COLUMN opinion,
    DROP COLUMN student,
    DROP COLUMN random,
    DROP COLUMN listen;

ALTER TABLE drops
    ADD COLUMN confession BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN funny BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN opinion BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN student BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN random BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN listen BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_drops_engagement_counters_non_negative
        CHECK (
            confession >= 0
            AND funny >= 0
            AND opinion >= 0
            AND student >= 0
            AND random >= 0
            AND listen >= 0
        );
